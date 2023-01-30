/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
    Authors: iText Software.

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
package com.itextpdf.layout.element;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.logs.LayoutLogMessageConstant;
import com.itextpdf.layout.properties.Background;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.IOException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class FlexContainerSplitTest extends ExtendedITextTest {

    private static final String SOURCE_FOLDER = "./src/test/resources/com/itextpdf/layout/FlexContainerSplitTest/";
    private static final String DESTINATION_FOLDER = "./target/test/com/itextpdf/layout/FlexContainerSplitTest/";

    private static final String VERY_LONG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do "
            + "eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud "
            + "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in "
            + "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat "
            + "cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. ";
    private static final String SHORT_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit,?";

    @BeforeClass
    public static void beforeClass() {
        createDestinationFolder(DESTINATION_FOLDER);
    }

    @Test
    public void simpleTest() throws IOException, InterruptedException {
        String outFileName = DESTINATION_FOLDER + "simpleTest.pdf";
        String cmpFileName = SOURCE_FOLDER + "cmp_simpleTest.pdf";
        try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outFileName))) {
            Document document = new Document(pdfDocument);
            pdfDocument.setDefaultPageSize(PageSize.A5);

            Div flexContainer = createDefaultFlexContainer();
            document.add(flexContainer);
        }

        Assert.assertNull(new CompareTool().compareByContent(outFileName, cmpFileName, DESTINATION_FOLDER, "diff"));
    }

    @Test
    public void heightPropertyTest() throws IOException, InterruptedException {
        String outFileName = DESTINATION_FOLDER + "heightPropertyTest.pdf";
        String cmpFileName = SOURCE_FOLDER + "cmp_heightPropertyTest.pdf";

        try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outFileName))) {
            Document document = new Document(pdfDocument);
            pdfDocument.setDefaultPageSize(PageSize.A5);

            Div flexContainer = createDefaultFlexContainer();
            ((Paragraph) flexContainer.getChildren().get(0)).setHeight(250);
            document.add(flexContainer);
        }

        Assert.assertNull(new CompareTool().compareByContent(outFileName, cmpFileName, DESTINATION_FOLDER, "diff"));
    }

    @Test
    public void smallTrailingElementTest() throws IOException, InterruptedException {
        String outFileName = DESTINATION_FOLDER + "smallTrailingElementTest.pdf";
        String cmpFileName = SOURCE_FOLDER + "cmp_smallTrailingElementTest.pdf";

        try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outFileName))) {
            Document document = new Document(pdfDocument);
            pdfDocument.setDefaultPageSize(PageSize.A5);

            Div flexContainer = createDefaultFlexContainer();
            ((Paragraph) flexContainer.getChildren().get(0)).setHeight(250);
            Paragraph p3 = new Paragraph(SHORT_TEXT)
                    .setWidth(UnitValue.createPercentValue(25))
                    .setBackgroundColor(ColorConstants.BLUE)
                    .setHeight(250);
            flexContainer.add(p3);
            document.add(flexContainer);
        }

        Assert.assertNull(new CompareTool().compareByContent(outFileName, cmpFileName, DESTINATION_FOLDER, "diff"));
    }

    @Test
    public void splitOverSeveralPagesTest() throws IOException, InterruptedException {
        String outFileName = DESTINATION_FOLDER + "splitOverSeveralPagesTest.pdf";
        String cmpFileName = SOURCE_FOLDER + "cmp_splitOverSeveralPagesTest.pdf";

        try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outFileName))) {
            Document document = new Document(pdfDocument);
            pdfDocument.setDefaultPageSize(PageSize.A6);

            Div flexContainer = createDefaultFlexContainer();
            document.add(flexContainer);
        }

        Assert.assertNull(new CompareTool().compareByContent(outFileName, cmpFileName, DESTINATION_FOLDER, "diff"));
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = LayoutLogMessageConstant.ELEMENT_DOES_NOT_FIT_AREA))
    public void keepTogetherIgnoredTest() throws IOException, InterruptedException {
        String outFileName = DESTINATION_FOLDER + "keepTogetherIgnoredTest.pdf";
        String cmpFileName = SOURCE_FOLDER + "cmp_keepTogetherIgnoredTest.pdf";

        try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outFileName))) {
            Document document = new Document(pdfDocument);
            pdfDocument.setDefaultPageSize(PageSize.A5);

            Div flexContainer = new FlexContainer();
            flexContainer.add(new Div().setWidth(50).setHeight(600).setBackgroundColor(ColorConstants.YELLOW))
                    .add(new Div().setWidth(50).setHeight(400).setBackgroundColor(ColorConstants.BLUE));
            flexContainer.setProperty(Property.KEEP_TOGETHER, true);
            document.add(flexContainer);
        }

        Assert.assertNull(new CompareTool().compareByContent(outFileName, cmpFileName, DESTINATION_FOLDER, "diff"));
    }

    private Div createDefaultFlexContainer() {
        Div flexContainer = new FlexContainer();
        flexContainer.setProperty(Property.BORDER, new SolidBorder(2));
        flexContainer.setProperty(Property.BACKGROUND, new Background(ColorConstants.LIGHT_GRAY));
        Paragraph p1 = new Paragraph(SHORT_TEXT)
                .setWidth(UnitValue.createPercentValue(25))
                .setBackgroundColor(ColorConstants.BLUE);
        p1.setProperty(Property.FLEX_GROW, 0f);
        p1.setProperty(Property.FLEX_SHRINK, 0f);
        flexContainer.add(p1);

        Paragraph p2 = new Paragraph(VERY_LONG_TEXT + VERY_LONG_TEXT + VERY_LONG_TEXT + VERY_LONG_TEXT)
                .setWidth(UnitValue.createPercentValue(75))
                .setBackgroundColor(ColorConstants.YELLOW);
        p2.setProperty(Property.FLEX_GROW, 1f);
        p2.setProperty(Property.FLEX_SHRINK, 1f);
        flexContainer.add(p2);

        return flexContainer;
    }
}