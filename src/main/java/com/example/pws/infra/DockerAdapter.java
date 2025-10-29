package com.example.pws.infra;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.core.DockerClientImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class DockerAdapter {

    private final DockerClient client;

    public DockerAdapter() {
        var config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        this.client = DockerClientImpl.getInstance(config, httpClient);
    }

    /** Vérifie ou télécharge l'image Docker */
    public String ensureImage(String image) {
        try {
            client.inspectImageCmd(image).exec();
        } catch (Exception e) {
            log.info("Pulling image {}...", image);
            try {
                client.pullImageCmd(image).start().awaitCompletion();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // bonne pratique : réinterrompt le thread
                log.error("Image pull interrupted for {}", image, ie);
            }
        }
        return image;
    }


    /** Crée et démarre un conteneur interactif */
    public String createInteractiveContainer(String image) {
        ensureImage(image);

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withNetworkMode("bridge")
                .withMemory((long) 256 * 1024 * 1024) // 256 MB
                .withCapDrop(Capability.SYS_ADMIN);

        CreateContainerResponse container = client.createContainerCmd(image)
                .withTty(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd("/bin/sh") // ou /bin/bash selon image
                .withHostConfig(hostConfig)
                .exec();

        client.startContainerCmd(container.getId()).exec();
        return container.getId();
    }

    /** Tue et supprime un conteneur */
    public void killAndRemove(String containerId) {
        try {
            client.killContainerCmd(containerId).exec();
        } catch (Exception ignored) {}
        try {
            client.removeContainerCmd(containerId).withForce(true).exec();
        } catch (Exception ignored) {}
    }

    /** Crée une commande exec pour attacher un shell */
    public ExecCreateCmdResponse createExec(String containerId) {
        return client.execCreateCmd(containerId)
                .withTty(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withCmd("/bin/sh")
                .exec();
    }

    /** Getter du client Docker */
    public DockerClient getClient() {
        return client;
    }
}
