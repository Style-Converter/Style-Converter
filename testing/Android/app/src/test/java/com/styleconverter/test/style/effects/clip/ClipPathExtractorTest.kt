package com.styleconverter.test.style.effects.clip

// Phase 8 functional tests for ClipPathExtractor — covering the four basic
// shape types (circle, ellipse, inset, polygon) plus the path("M…") form.
// Fixture shapes mirror the CSS parser's emission for clip-path longhands.

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipPathExtractorTest {

    private fun parse(s: String) = Json.parseToJsonElement(s)
    private fun pair(t: String, j: String) = t to parse(j)

    @Test
    fun `circle clip-path extracts Circle with percentage radius`() {
        // IR form mirrors fixture 047 in examples/properties/effects/clip-path-basic-shapes.json.
        val cfg = ClipPathExtractor.extractClipPathConfig(
            listOf(pair("ClipPath",
                "{\"type\":\"circle\",\"radius\":{\"percentage\":50.0},\"x\":50.0,\"y\":50.0}"))
        )
        val shape = cfg.shape
        assertTrue("expected Circle", shape is ClipShape.Circle)
        val circle = shape as ClipShape.Circle
        assertTrue(circle.radius is ClipRadius.Percentage)
    }

    @Test
    fun `ellipse clip-path extracts Ellipse with dual radii`() {
        val cfg = ClipPathExtractor.extractClipPathConfig(
            listOf(pair("ClipPath",
                "{\"type\":\"ellipse\",\"rx\":{\"percentage\":40.0},\"ry\":{\"percentage\":25.0},\"x\":50.0,\"y\":50.0}"))
        )
        assertTrue(cfg.shape is ClipShape.Ellipse)
    }

    @Test
    fun `inset clip-path extracts Inset with four edges`() {
        // inset(10px 20px 30px 40px) — four-argument form.
        val cfg = ClipPathExtractor.extractClipPathConfig(
            listOf(pair("ClipPath",
                "{\"type\":\"inset\"," +
                    "\"top\":{\"px\":10.0},\"right\":{\"px\":20.0}," +
                    "\"bottom\":{\"px\":30.0},\"left\":{\"px\":40.0}}"))
        )
        val shape = cfg.shape as ClipShape.Inset
        assertEquals(10f, shape.top.value, 0.01f)
        assertEquals(40f, shape.left.value, 0.01f)
    }

    @Test
    fun `polygon clip-path extracts all points`() {
        // Triangle polygon — three points.
        val cfg = ClipPathExtractor.extractClipPathConfig(
            listOf(pair("ClipPath",
                "{\"type\":\"polygon\",\"points\":[" +
                    "{\"x\":50.0,\"y\":0.0}," +
                    "{\"x\":100.0,\"y\":100.0}," +
                    "{\"x\":0.0,\"y\":100.0}" +
                "]}"))
        )
        val shape = cfg.shape as ClipShape.Polygon
        assertEquals(3, shape.points.size)
    }

    @Test
    fun `unknown clip-path type yields null shape`() {
        // Unsupported shape types don't crash — they just produce no clip.
        // This keeps the test-android pipeline resilient to IR drift.
        val cfg = ClipPathExtractor.extractClipPathConfig(
            listOf(pair("ClipPath", "{\"type\":\"bogus\"}"))
        )
        assertNull(cfg.shape)
        assertTrue(!cfg.hasClipPath)
    }

    @Test
    fun `empty properties produces empty config`() {
        val cfg = ClipPathExtractor.extractClipPathConfig(emptyList())
        assertNull(cfg.shape)
    }

    @Test
    fun `path clip-path captures raw SVG string`() {
        val cfg = ClipPathExtractor.extractClipPathConfig(
            listOf(pair("ClipPath", "{\"type\":\"path\",\"d\":\"M10 10 L 90 10 L 50 90 Z\"}"))
        )
        assertNotNull("Path shape must be extracted", cfg.shape)
        assertTrue(cfg.shape is ClipShape.Path)
    }
}
