/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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
package com.itextpdf.signatures.sign;

import com.itextpdf.bouncycastleconnector.BouncyCastleFactoryCreator;
import com.itextpdf.commons.bouncycastle.IBouncyCastleFactory;
import com.itextpdf.commons.bouncycastle.cert.ocsp.AbstractOCSPException;
import com.itextpdf.commons.bouncycastle.operator.AbstractOperatorCreationException;
import com.itextpdf.commons.bouncycastle.pkcs.AbstractPKCSException;
import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.forms.form.element.SignatureFieldAppearance;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.CrlClientOnline;
import com.itextpdf.signatures.IIssuingCertificateRetriever;
import com.itextpdf.signatures.IssuingCertificateRetriever;
import com.itextpdf.signatures.PdfPadesSigner;
import com.itextpdf.signatures.SignerProperties;
import com.itextpdf.signatures.TestSignUtils;
import com.itextpdf.signatures.testutils.PemFileHelper;
import com.itextpdf.signatures.testutils.client.AdvancedTestOcspClient;
import com.itextpdf.signatures.testutils.client.TestTsaClient;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Category(IntegrationTest.class)
public class PdfPadesMissingCertificatesTest extends ExtendedITextTest {
    private static final IBouncyCastleFactory FACTORY = BouncyCastleFactoryCreator.getFactory();

    private static final String certsSrc = "./src/test/resources/com/itextpdf/signatures/certs/";
    private static final String sourceFolder = "./src/test/resources/com/itextpdf/signatures/sign/PdfPadesMissingCertificatesTest/";
    private static final String destinationFolder = "./target/test/com/itextpdf/signatures/sign/PdfPadesMissingCertificatesTest/";
    private static final char[] password = "testpassphrase".toCharArray();

    @BeforeClass
    public static void before() {
        Security.addProvider(FACTORY.getProvider());
        createOrClearDestinationFolder(destinationFolder);
    }

    @Test
    public void retrieveMissingCertificatesTest() throws GeneralSecurityException, IOException,
            AbstractOperatorCreationException, AbstractPKCSException, AbstractOCSPException {
        String srcFileName = sourceFolder + "helloWorldDoc.pdf";
        String rootCertFileName = certsSrc + "root.pem";
        String intermediateCertFileName = certsSrc + "intermediate.pem";
        String signCertFileName = certsSrc + "sign.pem";
        String rootCrlFileName = certsSrc + "crlRoot.pem";
        String intermediateCrlFileName = certsSrc + "crlIntermediate.pem";
        String crlCertFileName = certsSrc + "crlCert.pem";
        String rootOcspFileName = certsSrc + "ocspRoot.pem";
        String intermediateOscpFileName = certsSrc + "ocspIntermediate.pem";
        String ocspCertFileName = certsSrc + "ocspCert.pem";
        String rootTsaFileName = certsSrc + "tsaRoot.pem";
        String intermediateTsaFileName = certsSrc + "tsaIntermediate.pem";
        String tsaCertFileName = certsSrc + "tsaCert.pem";
        String crlSignedByCA = certsSrc + "crlSignedByCA.crl";
        String crlSignedByCrlCert = certsSrc + "crlSignedByCrlCert.crl";

        X509Certificate signCert = (X509Certificate) PemFileHelper.readFirstChain(signCertFileName)[0];
        PrivateKey signPrivateKey = PemFileHelper.readFirstKey(signCertFileName, password);
        X509Certificate crlCert = (X509Certificate) PemFileHelper.readFirstChain(crlCertFileName)[0];
        X509Certificate ocspCert = (X509Certificate) PemFileHelper.readFirstChain(ocspCertFileName)[0];
        PrivateKey ocspPrivateKey = PemFileHelper.readFirstKey(ocspCertFileName, password);
        Certificate[] tsaChain = PemFileHelper.readFirstChain(tsaCertFileName);
        PrivateKey tsaPrivateKey = PemFileHelper.readFirstKey(tsaCertFileName, password);

        SignerProperties signerProperties = createSignerProperties();
        TestTsaClient testTsa = new TestTsaClient(Arrays.asList(tsaChain), tsaPrivateKey);

        CrlClientOnline testCrlClient = new CrlClientOnline() {
            @Override
            protected InputStream getCrlResponse(X509Certificate cert, URL urlt) throws IOException {
                if (urlt.toString().contains("sign-crl")) {
                    return FileUtil.getInputStreamForFile(crlSignedByCrlCert);
                }
                return FileUtil.getInputStreamForFile(crlSignedByCA);
            }
        };

        X509Certificate rootCert = (X509Certificate) PemFileHelper.readFirstChain(rootCertFileName)[0];
        X509Certificate crlRootCert = (X509Certificate) PemFileHelper.readFirstChain(rootCrlFileName)[0];
        X509Certificate ocspRootCert = (X509Certificate) PemFileHelper.readFirstChain(rootOcspFileName)[0];
        X509Certificate tsaRootCert = (X509Certificate) PemFileHelper.readFirstChain(rootTsaFileName)[0];
        X509Certificate crlIntermediateCert = (X509Certificate) PemFileHelper.readFirstChain(intermediateCrlFileName)[0];
        X509Certificate ocspIntermediateCert = (X509Certificate) PemFileHelper.readFirstChain(intermediateOscpFileName)[0];
        X509Certificate tsaIntermediateCert = (X509Certificate) PemFileHelper.readFirstChain(intermediateTsaFileName)[0];

        AdvancedTestOcspClient ocspClient = new AdvancedTestOcspClient(null);
        ocspClient.addBuilderForCertIssuer(signCert, ocspCert, ocspPrivateKey);
        ocspClient.addBuilderForCertIssuer(ocspIntermediateCert, ocspCert, ocspPrivateKey);
        ocspClient.addBuilderForCertIssuer(crlIntermediateCert, ocspCert, ocspPrivateKey);
        ocspClient.addBuilderForCertIssuer(tsaIntermediateCert, ocspCert, ocspPrivateKey);

        ocspClient.addBuilderForCertIssuer(rootCert, ocspCert, ocspPrivateKey);
        ocspClient.addBuilderForCertIssuer(crlRootCert, ocspCert, ocspPrivateKey);
        ocspClient.addBuilderForCertIssuer(ocspRootCert, ocspCert, ocspPrivateKey);
        ocspClient.addBuilderForCertIssuer(tsaRootCert, ocspCert, ocspPrivateKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfPadesSigner padesSigner =
                new PdfPadesSigner(new PdfReader(FileUtil.getInputStreamForFile(srcFileName)), outputStream);
        padesSigner.setCrlClient(testCrlClient);
        padesSigner.setOcspClient(ocspClient);
        IIssuingCertificateRetriever issuingCertificateRetriever = new IssuingCertificateRetriever() {
            @Override
            protected InputStream getIssuerCertByURI(String uri) throws IOException {
                if (uri.contains("crl_cert")) {
                    return FileUtil.getInputStreamForFile(crlCertFileName);
                }
                if (uri.contains("crl_intermediate")) {
                    return FileUtil.getInputStreamForFile(intermediateCrlFileName);
                }
                if (uri.contains("crl_root")) {
                    return FileUtil.getInputStreamForFile(rootCrlFileName);
                }
                if (uri.contains("tsa_intermediate")) {
                    return FileUtil.getInputStreamForFile(intermediateTsaFileName);
                }
                if (uri.contains("tsa_root")) {
                    return FileUtil.getInputStreamForFile(rootTsaFileName);
                }
                if (uri.contains("ocsp_intermediate")) {
                    return FileUtil.getInputStreamForFile(intermediateOscpFileName);
                }
                if (uri.contains("ocsp_root")) {
                    return FileUtil.getInputStreamForFile(rootOcspFileName);
                }
                if (uri.contains("intermediate")) {
                    return FileUtil.getInputStreamForFile(intermediateCertFileName);
                }
                return FileUtil.getInputStreamForFile(rootCertFileName);
            }
        };
        padesSigner.setIssuingCertificateRetriever(issuingCertificateRetriever);


        Certificate[] signChain = new Certificate[]{signCert};
        padesSigner.signWithBaselineLTProfile(signerProperties, signChain, signPrivateKey, testTsa);

        outputStream.close();

        TestSignUtils.basicCheckSignedDoc(new ByteArrayInputStream(outputStream.toByteArray()), "Signature1");

        Map<String, Integer> expectedNumberOfCrls = new HashMap<>();
        Map<String, Integer> expectedNumberOfOcsps = new HashMap<>();
        // It is expected to have two CRL responses, one for intermediate cert and another for leaf CRL/OCSP/TSA certs.
        expectedNumberOfCrls.put(crlCert.getSubjectX500Principal().getName(), 1);
        expectedNumberOfCrls.put(rootCert.getSubjectX500Principal().getName(), 1);
        // It is expected to have OCSP responses for all the root, CRL/OCSP/TSA intermediate certs, and signing cert.
        expectedNumberOfOcsps.put(ocspCert.getSubjectX500Principal().getName(), 8);
        TestSignUtils.assertDssDict(new ByteArrayInputStream(outputStream.toByteArray()),
                expectedNumberOfCrls, expectedNumberOfOcsps);
    }

    private SignerProperties createSignerProperties() {
        SignerProperties signerProperties = new SignerProperties();
        signerProperties.setFieldName("Signature1");
        SignatureFieldAppearance appearance = new SignatureFieldAppearance(signerProperties.getFieldName())
                .setContent("Approval test signature.\nCreated by iText.");
        signerProperties.setPageRect(new Rectangle(50, 650, 200, 100))
                .setSignatureAppearance(appearance);

        return signerProperties;
    }
}
