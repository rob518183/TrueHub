package com.imnotndesh.truehub.data.helpers

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

object ConnectionErrorFormatter {
    fun toUserMessage(
        throwable: Throwable?,
        serverUrl: String?,
        fallback: String = "Unable to establish connection"
    ): String {
        val endpoint = endpointFromUrl(serverUrl)
        val cause = rootCause(throwable)

        return when (cause) {
            is SocketTimeoutException -> {
                "Timed out connecting to $endpoint. Check protocol/port (ws vs wss), firewall, and server reachability."
            }

            is UnknownHostException -> {
                "Could not resolve server host for $endpoint. Verify the address and DNS/network settings."
            }

            is ConnectException -> {
                "Could not connect to $endpoint. Verify the port and that TrueNAS is reachable from this device."
            }

            is SSLHandshakeException, is SSLPeerUnverifiedException -> {
                "Secure connection failed for $endpoint. If using a self-signed cert, enable Insecure or trust the certificate."
            }

            else -> {
                val raw = cause?.message?.takeIf { it.isNotBlank() }
                if (raw != null) {
                    "Connection failed for $endpoint: $raw"
                } else {
                    "$fallback for $endpoint."
                }
            }
        }
    }

    private fun endpointFromUrl(serverUrl: String?): String {
        if (serverUrl.isNullOrBlank()) return "server"

        return try {
            val uri = URI(serverUrl)
            val host = uri.host ?: return serverUrl
            val port = if (uri.port != -1) uri.port else defaultPort(uri.scheme)
            "$host:$port"
        } catch (_: Exception) {
            serverUrl
        }
    }

    private fun defaultPort(scheme: String?): Int {
        return when (scheme?.lowercase()) {
            "wss" -> 443
            "ws" -> 80
            else -> -1
        }
    }

    private fun rootCause(throwable: Throwable?): Throwable? {
        var current = throwable
        while (current?.cause != null && current.cause != current) {
            current = current.cause
        }
        return current
    }
}