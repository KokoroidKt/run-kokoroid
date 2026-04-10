package com.example.kokoroidExample

import dev.kokoroidkt.driverApi.driver.Driver
import dev.kokoroidkt.driverApi.logger.getLogger

class ExampleDriver: Driver() {

    override fun onLoad() {
        getLogger().info { "ExampleDriver" }
        getLogger().info { "IDEA Debug is Supported" }
        getLogger().info { "onLoad() called" }
    }

    override fun onStart() {
        getLogger().info { "onStart() called" }
    }

    override fun onStop() {
        getLogger().info { "onStop() called" }

    }

    override fun onUnload() {
        getLogger().info { "onUnload() called" }
    }

}