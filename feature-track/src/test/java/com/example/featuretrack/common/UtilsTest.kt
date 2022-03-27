package com.example.featuretrack.common

import com.example.feature_track.R
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {
    @Test
    fun `test meterToKm`() {
        val provided = 4306.677f
        val expected = 4.3
        val result = Utils.meterToKiloMeter(provided)
        assertEquals(expected, result, 0.01)
    }

    @Test
    fun `test getVehicleDrawableId input ebicycle return correct drawable`() {
        val input = "ebicycle"
        val expected = R.drawable.ic_ebicycle
        val result = Utils.getVehicleDrawableId(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test getVehicleDrawableId input escooter return correct drawable`() {
        val input = "escooter"
        val expected = R.drawable.ic_scooter
        val result = Utils.getVehicleDrawableId(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test getVehicleDrawableId input emoped return correct drawable`() {
        val input = "emoped"
        val expected = R.drawable.ic_emoped
        val result = Utils.getVehicleDrawableId(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test getVehicleDrawableId input invalid string return vehicle drawable`() {
        val input = "invalid"
        val expected = R.drawable.ic_vehicle
        val result = Utils.getVehicleDrawableId(input)
        assertEquals(expected, result)
    }
}
