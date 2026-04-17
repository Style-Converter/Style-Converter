package com.styleconverter.test.style.core.types

// Unit tests for extractLength — at least one per documented IR shape quirk.
// Each shape snippet is copy-pasted from examples/primitives/*.json output.

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LengthValueTest {

    // Helper to keep test bodies small.
    private fun parse(s: String) = Json.parseToJsonElement(s)

    @Test fun `absolute px wrapped in type-length object`() {
        // Quirk 1: Width uses { type: "length", px: N }.
        val v = extractLength(parse("""{"type":"length","px":200.0}"""))
        assertEquals(LengthValue.Exact(200.0), v)
    }

    @Test fun `absolute raw px without type wrapper`() {
        // Quirk 1 (continued): PaddingTop emits { px: N }.
        val v = extractLength(parse("""{"px":8.0}"""))
        assertEquals(LengthValue.Exact(8.0), v)
    }

    @Test fun `non-px absolute unit normalized to px with original carried`() {
        // lengths-absolute.json: 120pt → px=160, original={v:120,u:PT}.
        val v = extractLength(parse("""{"type":"length","px":160.0,"original":{"v":120.0,"u":"PT"}}"""))
        assertEquals(LengthValue.Exact(160.0), v)
    }

    @Test fun `font-relative em has no px fallback`() {
        val v = extractLength(parse("""{"type":"length","original":{"v":2.0,"u":"EM"}}"""))
        assertEquals(LengthValue.Relative(2.0, LengthUnit.EM, null), v)
    }

    @Test fun `font-relative rem rlh lh cap ic ex ch all parse`() {
        for (u in listOf("REM", "LH", "RLH", "CAP", "IC", "EX", "CH")) {
            val v = extractLength(parse("""{"type":"length","original":{"v":1.5,"u":"$u"}}"""))
            assertEquals(LengthValue.Relative(1.5, LengthUnit.valueOf(u), null), v)
        }
    }

    @Test fun `classic viewport units parse`() {
        for (u in listOf("VW", "VH", "VMIN", "VMAX", "VI", "VB")) {
            val v = extractLength(parse("""{"type":"length","original":{"v":50.0,"u":"$u"}}"""))
            assertEquals(LengthValue.Relative(50.0, LengthUnit.valueOf(u), null), v)
        }
    }

    @Test fun `small large dynamic viewport units parse`() {
        // Covers the 18 new viewport units the spec calls out.
        val units = listOf(
            "SVW", "SVH", "SVMIN", "SVMAX", "SVI", "SVB",
            "LVW", "LVH", "LVMIN", "LVMAX", "LVI", "LVB",
            "DVW", "DVH", "DVMIN", "DVMAX", "DVI", "DVB",
        )
        for (u in units) {
            val v = extractLength(parse("""{"type":"length","original":{"v":10.0,"u":"$u"}}"""))
            assertEquals(LengthValue.Relative(10.0, LengthUnit.valueOf(u), null), v)
        }
    }

    @Test fun `container-query units parse`() {
        for (u in listOf("CQW", "CQH", "CQI", "CQB", "CQMIN", "CQMAX")) {
            val v = extractLength(parse("""{"type":"length","original":{"v":50.0,"u":"$u"}}"""))
            assertEquals(LengthValue.Relative(50.0, LengthUnit.valueOf(u), null), v)
        }
    }

    @Test fun `intrinsic auto is bare string data`() {
        // Quirk 3.
        val v = extractLength(parse(""""auto""""))
        assertEquals(LengthValue.Auto, v)
    }

    @Test fun `intrinsic min-content and max-content bare strings`() {
        assertEquals(
            LengthValue.Intrinsic(LengthValue.IntrinsicKind.MIN_CONTENT),
            extractLength(parse(""""min-content"""")),
        )
        assertEquals(
            LengthValue.Intrinsic(LengthValue.IntrinsicKind.MAX_CONTENT),
            extractLength(parse(""""max-content"""")),
        )
    }

    @Test fun `percentage sizing uses its own wrapper shape`() {
        // Quirk 2: { type: "percentage", value: 50.0 } NOT an IRLength.
        val v = extractLength(parse("""{"type":"percentage","value":50.0}"""))
        assertEquals(LengthValue.Relative(50.0, LengthUnit.PERCENT, null), v)
    }

    @Test fun `grid fraction is Fraction variant`() {
        // Quirk 4: fr is grid-track shape, not an IRLength.
        val v = extractLength(parse("""{"fr":2.0}"""))
        assertEquals(LengthValue.Fraction(2.0), v)
    }

    @Test fun `Q unit normalizes to px like other absolutes`() {
        // Absolute units always land in Exact even when the IR keeps original.
        val v = extractLength(parse("""{"type":"length","px":18.89763779527559,"original":{"v":80.0,"u":"Q"}}"""))
        assertTrue(v is LengthValue.Exact)
    }

    @Test fun `unknown unit string degrades to UNKNOWN`() {
        // Defensive: an unrecognized unit keeps the value but tags UNKNOWN.
        val v = extractLength(parse("""{"type":"length","original":{"v":3.0,"u":"PARSECS"}}"""))
        assertEquals(LengthValue.Relative(3.0, LengthUnit.UNKNOWN, null), v)
    }

    @Test fun `null input returns Unknown not null`() {
        assertEquals(LengthValue.Unknown, extractLength(null))
    }

    @Test fun `empty object returns Unknown`() {
        assertEquals(LengthValue.Unknown, extractLength(parse("""{}""")))
    }

    @Test fun `arbitrary non-keyword string returns Unknown`() {
        assertEquals(LengthValue.Unknown, extractLength(parse(""""nonsense"""")))
    }
}
