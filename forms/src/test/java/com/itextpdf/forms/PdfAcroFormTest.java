/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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
package com.itextpdf.forms;

import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfTextFormField;
import com.itextpdf.forms.fields.TextFormFieldBuilder;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfBoolean;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfVersion;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.UnitTest;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class PdfAcroFormTest extends ExtendedITextTest {

    public static final String destinationFolder = "./target/test/com/itextpdf/forms/PdfAcroFormTest/";
    public static final String sourceFolder = "./src/test/resources/com/itextpdf/forms/PdfAcroFormTest/";


    @Test
    public void setSignatureFlagsTest() {
        try(PdfDocument outputDoc = createDocument()) {
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            acroForm.setSignatureFlags(65);

            boolean isModified = acroForm.getPdfObject().isModified();
            boolean isReleaseForbidden = acroForm.getPdfObject().isReleaseForbidden();
            PdfObject sigFlags = acroForm.getPdfObject().get(PdfName.SigFlags);
            outputDoc.close();

            Assert.assertEquals(new PdfNumber(65), sigFlags);
            Assert.assertTrue(isModified);
            Assert.assertTrue(isReleaseForbidden);
        }
    }

    @Test
    public void addChildToFormFieldTest() {
        try (PdfDocument outputDoc = createDocument()) {
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            PdfFormField field = new TextFormFieldBuilder(outputDoc, "text1")
                    .setWidgetRectangle(new Rectangle(100, 700, 200, 20)).createText();
            acroForm.addField(field);
            PdfFormField root = new TextFormFieldBuilder(outputDoc, "root")
                    .setWidgetRectangle(new Rectangle(100, 600, 200, 20)).createText();
            PdfFormField child = new TextFormFieldBuilder(outputDoc, "child")
                    .setWidgetRectangle(new Rectangle(100, 300, 200, 20)).createText();
            root.addKid(child);
            acroForm.addField(root);
            Assert.assertEquals(2, acroForm.fields.size());
            PdfArray fieldKids = root.getKids();
            Assert.assertEquals(2, fieldKids.size());
        }
    }

    @Test
    public void addChildToWidgetTest() {
        try (PdfDocument outputDoc = createDocument()) {
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            PdfArray options = new PdfArray();
            options.add(new PdfString("1"));
            options.add(new PdfString("2"));
            PdfTextFormField text = new TextFormFieldBuilder(outputDoc, "text")
                    .setWidgetRectangle(new Rectangle(36, 696, 20, 20)).createText();
            PdfTextFormField childText = new TextFormFieldBuilder(outputDoc, "childText")
                    .setWidgetRectangle(new Rectangle(36, 696, 20, 20)).createText();
            text.addKid(childText);
            acroForm.addField(text);
            Assert.assertEquals(1, acroForm.fields.size());
            List<PdfFormField> fieldKids = text.getChildFields();
            Assert.assertEquals(2, fieldKids.size());
        }
    }

    @Test
    public void getFormFieldChildTest() {
        try(PdfDocument outputDoc = createDocument()) {
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            PdfFormField field = new TextFormFieldBuilder(outputDoc, "text1")
                    .setWidgetRectangle(new Rectangle(100, 700, 200, 20)).createText();
            acroForm.addField(field);

            PdfFormField root = new TextFormFieldBuilder(outputDoc, "root")
                    .setWidgetRectangle(new Rectangle(100, 600, 200, 20)).createText();
            PdfFormField child = new TextFormFieldBuilder(outputDoc, "child")
                    .setWidgetRectangle(new Rectangle(100, 600, 200, 20)).createText();
            root.addKid(child);
            acroForm.addField(root);
            PdfFormField childField = acroForm.getField("root.child");
            Assert.assertEquals("root.child", childField.getFieldName().toString());
        }
    }

    @Test
    public void getFormFieldWithEqualChildNamesTest() {
        try(PdfDocument outputDoc = createDocument()) {
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            PdfFormField field = new TextFormFieldBuilder(outputDoc, "text1")
                    .setWidgetRectangle(new Rectangle(100, 700, 200, 20)).createText();
            acroForm.addField(field);

            PdfFormField root = new TextFormFieldBuilder(outputDoc, "root")
                    .setWidgetRectangle(new Rectangle(100, 600, 200, 20)).createText();
            PdfFormField child = new TextFormFieldBuilder(outputDoc, "field")
                    .setWidgetRectangle(new Rectangle(100, 300, 200, 20)).createText();
            PdfFormField child1 = new TextFormFieldBuilder(outputDoc, "field")
                    .setWidgetRectangle(new Rectangle(100, 300, 200, 20)).createText();
            PdfFormField child2 = new TextFormFieldBuilder(outputDoc, "another_name")
                    .setWidgetRectangle(new Rectangle(100, 300, 200, 20)).createText();
            child1.addKid(child2);
            child.addKid(child1);
            root.addKid(child);
            acroForm.addField(root);
            PdfFormField childField = acroForm.getField("root.field.field.another_name");
            Assert.assertEquals("root.field.field.another_name", childField.getFieldName().toString());
        }
    }

    @Test
    public void changeFieldNameTest() {
        try(PdfDocument outputDoc = createDocument()) {
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            PdfFormField field = new TextFormFieldBuilder(outputDoc, "text1")
                    .setWidgetRectangle(new Rectangle(100, 700, 200, 20)).createText();
            acroForm.addField(field);

            PdfFormField root = new TextFormFieldBuilder(outputDoc, "root")
                    .setWidgetRectangle(new Rectangle(100, 600, 200, 20)).createText();
            PdfFormField child = new TextFormFieldBuilder(outputDoc, "child")
                    .setWidgetRectangle(new Rectangle(100, 300, 200, 20)).createText();
            root.addKid(child);
            acroForm.addField(root);
            acroForm.getField("root").setFieldName("diff");
            PdfFormField childField = PdfAcroForm.getAcroForm(outputDoc, true).getField("diff.child");
            Assert.assertEquals("diff.child", childField.getFieldName().toString());
        }
    }

    @Test
    public void removeChildFromFormFieldTest() throws FileNotFoundException {
        try(PdfDocument outputDoc = createDocument()) {
            outputDoc.addNewPage();
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            PdfFormField field = new TextFormFieldBuilder(outputDoc, "text1")
                    .setWidgetRectangle(new Rectangle(100, 700, 200, 20)).createText().setValue("text1");
            acroForm.addField(field);

            PdfFormField root = new TextFormFieldBuilder(outputDoc, "root")
                    .setWidgetRectangle(new Rectangle(100, 600, 200, 20)).createText().setValue("root");
            PdfFormField child = new TextFormFieldBuilder(outputDoc, "child")
                    .setWidgetRectangle(new Rectangle(100, 300, 200, 20)).createText().setValue("child");
            PdfFormField child1 = new TextFormFieldBuilder(outputDoc, "aaaaa")
                    .setWidgetRectangle(new Rectangle(100, 400, 200, 20)).createText().setValue("aaaaa");
            PdfFormField child2 = new TextFormFieldBuilder(outputDoc, "bbbbb")
                    .setWidgetRectangle(new Rectangle(100, 500, 200, 20)).createText().setValue("bbbbb");
            child1.addKid(child2);
            child.addKid(child1);
            root.addKid(child);
            acroForm.addField(root);
            acroForm.removeField("root.child.aaaaa");
            Assert.assertEquals(2, acroForm.fields.size());
            Assert.assertEquals(2, root.getKids().size());
        }
    }

    @Test
    public void getChildFromFormFieldWithDifferentAmountOfChildrenTest() throws FileNotFoundException {
        try(PdfDocument outputDoc = createDocument()) {
            outputDoc.addNewPage();
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            PdfFormField field = new TextFormFieldBuilder(outputDoc, "text1")
                    .setWidgetRectangle(new Rectangle(100, 700, 200, 20)).createText().setValue("text1");
            acroForm.addField(field);

            PdfFormField root = new TextFormFieldBuilder(outputDoc, "root")
                    .setWidgetRectangle(new Rectangle(100, 600, 200, 20)).createText().setValue("root");
            PdfFormField child = new TextFormFieldBuilder(outputDoc, "child")
                    .setWidgetRectangle(new Rectangle(100, 300, 200, 20)).createText().setValue("child");
            PdfFormField child1 = new TextFormFieldBuilder(outputDoc, "aaaaa")
                    .setWidgetRectangle(new Rectangle(100, 400, 200, 20)).createText().setValue("aaaaa");
            PdfFormField child2 = new TextFormFieldBuilder(outputDoc, "bbbbb")
                    .setWidgetRectangle(new Rectangle(100, 500, 200, 20)).createText().setValue("bbbbb");
            PdfFormField child3 = new TextFormFieldBuilder(outputDoc, "child1")
                    .setWidgetRectangle(new Rectangle(100, 500, 200, 20)).createText().setValue("child1");
            PdfFormField child4 = new TextFormFieldBuilder(outputDoc, "child2")
                    .setWidgetRectangle(new Rectangle(100, 500, 200, 20)).createText().setValue("child2");
            PdfFormField child5 = new TextFormFieldBuilder(outputDoc, "child2")
                    .setWidgetRectangle(new Rectangle(100, 500, 200, 20)).createText().setValue("child2");
            child1.addKid(child2);
            child1.addKid(child3);
            child1.addKid(child4);
            child4.addKid(child5);
            child.addKid(child1);
            root.addKid(child);
            acroForm.addField(root);
            PdfFormField childField = acroForm.getField("root.child.aaaaa.child2");
            Assert.assertEquals("root.child.aaaaa.child2", childField.getFieldName().toString());

            Assert.assertEquals(2, acroForm.fields.size());
            Assert.assertEquals(2, root.getKids().size());
        }
    }

    @Test
    public void setCalculationOrderTest() {
        try (PdfDocument outputDoc = createDocument()) {

            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            PdfArray calculationOrderArray = new PdfArray(new int[] {1, 0});
            acroForm.setCalculationOrder(calculationOrderArray);

            boolean isModified = acroForm.getPdfObject().isModified();
            boolean isReleaseForbidden = acroForm.getPdfObject().isReleaseForbidden();
            PdfObject calculationOrder = acroForm.getPdfObject().get(PdfName.CO);
            outputDoc.close();

            Assert.assertEquals(calculationOrderArray, calculationOrder);
            Assert.assertTrue(isModified);
            Assert.assertTrue(isReleaseForbidden);
        }
    }

    @Test
    public void setDefaultAppearanceTest() {
        try (PdfDocument outputDoc = createDocument()) {

            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            acroForm.setDefaultAppearance("default appearance");

            boolean isModified = acroForm.getPdfObject().isModified();
            boolean isReleaseForbidden = acroForm.getPdfObject().isReleaseForbidden();
            PdfObject calculationOrder = acroForm.getPdfObject().get(PdfName.DA);
            outputDoc.close();

            Assert.assertEquals(new PdfString("default appearance"), calculationOrder);
            Assert.assertTrue(isModified);
            Assert.assertTrue(isReleaseForbidden);
        }
    }

    @Test
    public void setDefaultJustificationTest() {
        try (PdfDocument outputDoc = createDocument()) {

            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            acroForm.setDefaultJustification(14);

            boolean isModified = acroForm.getPdfObject().isModified();
            boolean isReleaseForbidden = acroForm.getPdfObject().isReleaseForbidden();
            PdfObject defaultJustification = acroForm.getPdfObject().get(PdfName.Q);
            outputDoc.close();

            Assert.assertEquals(new PdfNumber(14), defaultJustification);
            Assert.assertTrue(isModified);
            Assert.assertTrue(isReleaseForbidden);
        }
    }

    @Test
    public void setDefaultResourcesTest() {
        try (PdfDocument outputDoc = createDocument()) {

            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);

            PdfDictionary dictionary = new PdfDictionary();
            PdfAcroForm.getAcroForm(outputDoc, true).setDefaultResources(dictionary);

            boolean isModified = acroForm.getPdfObject().isModified();
            boolean isReleaseForbidden = acroForm.getPdfObject().isReleaseForbidden();
            PdfObject defaultResourcesDict = acroForm.getPdfObject().get(PdfName.DR);
            outputDoc.close();

            Assert.assertEquals(dictionary, defaultResourcesDict);
            Assert.assertTrue(isModified);
            Assert.assertTrue(isReleaseForbidden);
        }
    }

    @Test
    public void setNeedAppearancesTest() {
        try (PdfDocument outputDoc = createDocument()) {

            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            acroForm.setNeedAppearances(false);

            boolean isModified = acroForm.getPdfObject().isModified();
            boolean isReleaseForbidden = acroForm.getPdfObject().isReleaseForbidden();
            PdfObject needAppearance = acroForm.getPdfObject().get(PdfName.NeedAppearances);

            outputDoc.close();

            Assert.assertEquals(new PdfBoolean(false), needAppearance);
            Assert.assertTrue(isModified);
            Assert.assertTrue(isReleaseForbidden);
        }
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = "NeedAppearances has been deprecated in PDF 2.0. Appearance streams are required in PDF 2.0."))
    public void setNeedAppearancesInPdf2Test() {
        PdfDocument outputDoc = new PdfDocument(
                new PdfWriter(new ByteArrayOutputStream(), new WriterProperties().setPdfVersion(
                        PdfVersion.PDF_2_0)));
        outputDoc.addNewPage();

        PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
        acroForm.setNeedAppearances(false);

        boolean isModified = acroForm.getPdfObject().isModified();
        boolean isReleaseForbidden = acroForm.getPdfObject().isReleaseForbidden();
        PdfObject needAppearance = acroForm.getPdfObject().get(PdfName.NeedAppearances);

        outputDoc.close();

        Assert.assertNull(needAppearance);
        Assert.assertTrue(isModified);
        Assert.assertTrue(isReleaseForbidden);
    }

    @Test
    public void setGenerateAppearanceTest() {
        try (PdfDocument outputDoc = createDocument()) {

            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            acroForm.setNeedAppearances(false);
            acroForm.setGenerateAppearance(true);

            boolean isModified = acroForm.getPdfObject().isModified();
            boolean isReleaseForbidden = acroForm.getPdfObject().isReleaseForbidden();
            boolean isGenerateAppearance = acroForm.isGenerateAppearance();
            Object needAppearances = acroForm.getPdfObject().get(PdfName.NeedAppearances);
            outputDoc.close();

            Assert.assertNull(needAppearances);
            Assert.assertTrue(isGenerateAppearance);
            Assert.assertTrue(isModified);
            Assert.assertTrue(isReleaseForbidden);
        }
    }

    @Test
    public void setXFAResourcePdfArrayTest() {
        try (PdfDocument outputDoc = createDocument()) {

            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            PdfArray array = new PdfArray();
            acroForm.setXFAResource(array);

            boolean isModified = acroForm.getPdfObject().isModified();
            boolean isReleaseForbidden = acroForm.getPdfObject().isReleaseForbidden();
            PdfObject xfaObject = acroForm.getPdfObject().get(PdfName.XFA);
            outputDoc.close();

            Assert.assertEquals(array, xfaObject);
            Assert.assertTrue(isModified);
            Assert.assertTrue(isReleaseForbidden);
        }
    }

    @Test
    public void setXFAResourcePdfStreamTest() {
        try (PdfDocument outputDoc = createDocument()) {

            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(outputDoc, true);
            PdfStream stream = new PdfStream();
            acroForm.setXFAResource(stream);

            boolean isModified = acroForm.getPdfObject().isModified();
            boolean isReleaseForbidden = acroForm.getPdfObject().isReleaseForbidden();
            PdfObject xfaObject = acroForm.getPdfObject().get(PdfName.XFA);
            outputDoc.close();

            Assert.assertEquals(stream, xfaObject);
            Assert.assertTrue(isModified);
            Assert.assertTrue(isReleaseForbidden);
        }
    }

    private static PdfDocument createDocument() {
        PdfDocument outputDoc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
        outputDoc.addNewPage();
        return outputDoc;
    }
}
