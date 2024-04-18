/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.io.font;

import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class Type1FontTest extends ExtendedITextTest {

    @Test
    public void fillUsingEncodingTest() throws IOException {
        FontEncoding fontEncoding = FontEncoding.createFontEncoding("WinAnsiEncoding");
        Type1Font type1StdFont = (Type1Font) FontProgramFactory.createFont("Helvetica", true);
        Assert.assertEquals(149, type1StdFont.codeToGlyph.size());
        type1StdFont.initializeGlyphs(fontEncoding);
        Assert.assertEquals(217, type1StdFont.codeToGlyph.size());
        Assert.assertEquals(0x2013, type1StdFont.codeToGlyph.get(150).getUnicode());
        Assert.assertArrayEquals(new char[]{(char)0x2013}, type1StdFont.codeToGlyph.get(150).getChars());
    }
}
