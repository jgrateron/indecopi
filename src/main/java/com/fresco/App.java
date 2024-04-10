package com.fresco;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

public class App {

    public static String FILE_PEM = "/tmp/indecopi.pem";
    public static String FILE_P12 = "/tmp/indecopi.p12";

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException,
            XPathExpressionException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        var urlTsl = new URL("https://iofe.indecopi.gob.pe/TSL/tsl-pe.xml");
        try (var inTsl = iSToBAIS(urlTsl.openStream())) {
            var parseXml = new ParseXml(inTsl);
            var listCertificates = getCertificates(parseXml);
            saveToPem(listCertificates);
            saveToKeyStore(listCertificates);
            System.out.println("export " + listCertificates.size() + " certificates");
        }
    }

    private static void saveToKeyStore(List<String> listCertificates) throws KeyStoreException, FileNotFoundException,
            IOException, NoSuchAlgorithmException, CertificateException {
        var ks = KeyStore.getInstance("PKCS12");
        ks.load(null, "".toCharArray());
        for (var certificate : listCertificates) {
            var x509Certificate = getX509Cert(certificate);
            String alias = x509Certificate.getSubjectX500Principal().getName("RFC1779");
            ks.setCertificateEntry(alias, x509Certificate);
        }
        try (FileOutputStream fos = new FileOutputStream(FILE_P12)) {
            ks.store(fos, "".toCharArray());
        }
    }

    private static void saveToPem(List<String> listCertificates) throws FileNotFoundException, IOException {
        var sb = new StringBuilder();
        for (var certificate : listCertificates) {
            sb.append(certificate);
        }
        var file = new File(FILE_PEM);
        try (var fos = new FileOutputStream(file)) {
            fos.write(sb.toString().getBytes());
            fos.flush();
        }
    }

    private static List<String> getCertificates(ParseXml parseXml) throws XPathExpressionException {
        var result = new ArrayList<String>();
        var nodeList = parseXml.listNode(
                "/TrustServiceStatusList/TrustServiceProviderList/TrustServiceProvider/TSPServices/TSPService/ServiceInformation/ServiceDigitalIdentity/DigitalId/X509Certificate");
        for (var nodeX509Certificate : nodeList) {
            var strCertificate = parseXml.nodeValue(nodeX509Certificate);
            strCertificate = "-----BEGIN CERTIFICATE-----\n" + strCertificate + "\n-----END CERTIFICATE-----\n";
            result.add(strCertificate);
        }
        return result;
    }

    private static InputStream iSToBAIS(InputStream openStream) throws IOException {
        var baos = new ByteArrayOutputStream();
        openStream.transferTo(baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static X509Certificate getX509Cert(String cert) throws CertificateException {
        var certFactory = CertificateFactory.getInstance("X.509");
        var certX509 = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(cert.getBytes()));
        return certX509;
    }
}
