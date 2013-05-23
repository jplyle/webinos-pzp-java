/**
 * 
 */
package org.webinos.pzp.tls.certificates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.KeyStore.ProtectionParameter;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.jcajce.provider.asymmetric.x509.PEMUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMUtilities;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;

/**
 * @author johl
 * 
 */
public class CertificateManager {

	private static AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder()
			.find("SHA1withRSA");
	private static AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder()
			.find(sigAlgId);
	private static String PZP_COMMON_NAME = "WebinosPZP";
	private static String CLIENT_CN = PZP_COMMON_NAME + "-" + "CLIENT";
	private static String SERVER_CN = PZP_COMMON_NAME + "-" + "SERVER";

	
	
	private static CertificateManager instance = null;
	
	
	private KeyStore keyStore = null;
	private File keyStoreFile = null;
	private String keyStoreSecret = null;

	private CertificateManager(File file, String password) {
		Security.addProvider(new BouncyCastleProvider());
		this.keyStoreFile = file;
		this.keyStoreSecret = password;
	}

	private CertificateManager()  {
		
	}
	
	public void setKeyStoreFile(File f) {
		this.keyStoreFile = f;
	}
	
	public void setKeyStoreSecret(String secret) {
		this.keyStoreSecret = secret;
	}
	
	/**
	 * @param f
	 * 
	 *            Populates a KeyStore with
	 * 
	 *            - A PZP Master Key - A PZP Master self-signed certificate - A
	 *            PZP Master CSR, to be signed by a PZH - A PZP server key &
	 *            certificate (signed by the master) - A PZP client key &
	 *            certificate (signed by the master)
	 * 
	 * @throws CertificateManagerException
	 * @throws InvalidKeySpecException
	 */
	private KeyStore populateKeyStore(KeyStore keyStore, String secret)
			throws CertificateManagerException {
		try {
			AsymmetricCipherKeyPair masterKeys;
			KeyPair masterKeysJCE;
			X509CertificateHolder masterCertHolder;
			X509Certificate masterCertJCE;

			AsymmetricCipherKeyPair serverConnectionKeys;
			KeyPair serverConnectionKeysJCE;
			X509CertificateHolder serverConnectionCertHolder;
			X509Certificate serverConnectionCertJCE;

			AsymmetricCipherKeyPair clientConnectionKeys;
			KeyPair clientConnectionKeysJCE;
			X509CertificateHolder clientConnectionCertHolder;
			X509Certificate clientConnectionCertJCE;

			// create master key
			masterKeys = createKeyPair();
			masterKeysJCE = new KeyPair(
					convertBctoJcePublic(masterKeys.getPublic()),
					convertBctoJcePrivate(masterKeys.getPrivate()));
			masterCertHolder = createCertificate(PZP_COMMON_NAME,
					masterKeys.getPrivate(), masterKeys.getPublic());
			masterCertJCE = convertBCtoJCECert(masterCertHolder);
			Certificate[] masterCertChain = { masterCertJCE };
			// add master key to the keyStore
			PrivateKeyEntry privEntry = new PrivateKeyEntry(
					masterKeysJCE.getPrivate(), masterCertChain);
			ProtectionParameter params = new PasswordProtection(
					secret.toCharArray());
			keyStore.setEntry("pzp-master", privEntry, params);

			// create server key
			serverConnectionKeys = createKeyPair();
			serverConnectionKeysJCE = new KeyPair(
					convertBctoJcePublic(serverConnectionKeys.getPublic()),
					convertBctoJcePrivate(serverConnectionKeys.getPrivate()));
			serverConnectionCertHolder = createCertificate(SERVER_CN,
					masterKeys.getPrivate(), serverConnectionKeys.getPublic());
			serverConnectionCertJCE = convertBCtoJCECert(serverConnectionCertHolder);
			Certificate[] serverCertChain = { serverConnectionCertJCE,
					masterCertJCE };
			// add server key to the keyStore
			privEntry = new PrivateKeyEntry(
					serverConnectionKeysJCE.getPrivate(), serverCertChain);
			params = new PasswordProtection(secret.toCharArray());
			keyStore.setEntry("pzp-server", privEntry, params);

			// create client key
			clientConnectionKeys = createKeyPair();
			clientConnectionKeysJCE = new KeyPair(
					convertBctoJcePublic(clientConnectionKeys.getPublic()),
					convertBctoJcePrivate(clientConnectionKeys.getPrivate()));
			clientConnectionCertHolder = createCertificate(CLIENT_CN,
					masterKeys.getPrivate(), clientConnectionKeys.getPublic());
			clientConnectionCertJCE = convertBCtoJCECert(clientConnectionCertHolder);
			Certificate[] clientCertChain = { clientConnectionCertJCE,
					masterCertJCE };
			// add client key to the keyStore
			privEntry = new PrivateKeyEntry(
					clientConnectionKeysJCE.getPrivate(), clientCertChain);
			params = new PasswordProtection(secret.toCharArray());
			keyStore.setEntry("pzp-client", privEntry, params);
			return keyStore;
		} catch (NoSuchAlgorithmException | NoSuchProviderException
				| OperatorCreationException | IOException
				| CertificateException | KeyStoreException
				| InvalidKeySpecException e) {
			e.printStackTrace();
			throw new CertificateManagerException("Could not create keys", e);
		}
	}

	private boolean hasPopulatedKeyStore(KeyStore ks) {
		try {
			return ks != null && ks.containsAlias("pzp-client")
					&& ks.containsAlias("pzp-server")
					&& ks.containsAlias("pzp-master");
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return false;
		}
	}

	private String createCSR(AsymmetricCipherKeyPair masterKeys)
			throws IOException, OperatorCreationException {
		PKCS10CertificationRequest masterCSR;
		masterCSR = createCSR(PZP_COMMON_NAME, masterKeys.getPrivate(),
				masterKeys.getPublic());
		return toPemString(masterCSR);
	}

	private String toPemString(Object obj) throws IOException {
		StringWriter writer = new StringWriter();
		PEMWriter pemWriter = new PEMWriter(writer);
		pemWriter.writeObject(obj);
		pemWriter.flush();
		return writer.getBuffer().toString();
	}

	/**
	 * Takes the populated keystore and adds certificates, including:
	 *   - the PZH master certificate
	 *   - the PZH-signed certificate for the PZP master certificate
	 * @throws KeyStoreException 
	 * @throws UnrecoverableEntryException 
	 * @throws NoSuchAlgorithmException 
	 */
	private void addPZHCertificatesToStore(X509Certificate pzhMaster, X509Certificate pzpMaster) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
		assert(this.isPopulated());
		
		ProtectionParameter params = new PasswordProtection(this.keyStoreSecret.toCharArray());
		PrivateKeyEntry oldMasterKeyEntry = (PrivateKeyEntry) this.keyStore.getEntry("pzp-master", params);
		
		// replace the current Master Key entry with the pzpMaster
		// and add the pzhMaster
		Certificate[] newChain = { pzpMaster, pzhMaster };
		PrivateKeyEntry newPzpMaster = new PrivateKeyEntry(oldMasterKeyEntry.getPrivateKey(), newChain );
		this.keyStore.deleteEntry("pzp-master");
		this.keyStore.setEntry("pzp-master", newPzpMaster, params);
		
		PrivateKeyEntry oldClientKeyEntry = (PrivateKeyEntry) this.keyStore.getEntry("pzp-client", params);
		Certificate[] newClientChain = { oldClientKeyEntry.getCertificate(), pzpMaster, pzhMaster };
		PrivateKeyEntry newPzpClient = new PrivateKeyEntry(oldClientKeyEntry.getPrivateKey(), newClientChain);
		this.keyStore.deleteEntry("pzp-client");
		this.keyStore.setEntry("pzp-client", newPzpClient, params);
		
		PrivateKeyEntry oldServerKeyEntry = (PrivateKeyEntry) this.keyStore.getEntry("pzp-server", params);
		Certificate[] newServerChain = { oldServerKeyEntry.getCertificate(), pzpMaster, pzhMaster };
		PrivateKeyEntry newPzpServer = new PrivateKeyEntry(oldServerKeyEntry.getPrivateKey(), newServerChain);
		this.keyStore.deleteEntry("pzp-server");
		this.keyStore.setEntry("pzp-server", newPzpServer, params);
	}

	private X509CertificateHolder createCertificate(String subjectCommonName,
			AsymmetricKeyParameter signingKey, AsymmetricKeyParameter subjectKey)
			throws OperatorCreationException, IOException,
			NoSuchAlgorithmException, NoSuchProviderException {

		ContentSigner sigGen = (new BcRSAContentSignerBuilder(sigAlgId,
				digAlgId)).build(signingKey);
		SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfoFactory
				.createSubjectPublicKeyInfo(subjectKey);

		Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60
				* 1000);
		Calendar c = Calendar.getInstance();
		c.add(Calendar.YEAR, 1);

		Date endDate = c.getTime();
		// new Date(System.currentTimeMillis() + 365 * 365 * 24 * 60 * 60 *
		// 1000);

		SecureRandom sr2 = SecureRandom.getInstance("SHA1PRNG", "SUN");
		byte[] randomBytes = new byte[100];
		sr2.nextBytes(randomBytes);
		BigInteger randomBig = new BigInteger(randomBytes);

		X509v3CertificateBuilder v3CertGen = new X509v3CertificateBuilder(
				new X500Name("CN=" + PZP_COMMON_NAME
						+ ", OU=WP4, O=Webinos, C=London S=MX C=UK"),
				randomBig, startDate, endDate, new X500Name("CN="
						+ subjectCommonName
						+ ", OU=WP4, O=Webinos, C=London S=MX C=UK"),
				subPubKeyInfo);

		X509CertificateHolder certHolder = v3CertGen.build(sigGen);

		return certHolder;
	}

	private PKCS10CertificationRequest createCSR(String subjectCommonName,
			AsymmetricKeyParameter signingKey, AsymmetricKeyParameter subjectKey)
			throws IOException, OperatorCreationException {

		SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfoFactory
				.createSubjectPublicKeyInfo(subjectKey);

		PKCS10CertificationRequestBuilder builder = new PKCS10CertificationRequestBuilder(
				new X500Name("CN=" + subjectCommonName
						+ ", OU=WP4, O=Webinos, C=London S=MX C=UK"),
				subPubKeyInfo);

		ContentSigner sigGen = (new BcRSAContentSignerBuilder(sigAlgId,
				digAlgId)).build(signingKey);

		return builder.build(sigGen);
	}

	private KeyStore createEmptyKeyStore(File file, String password)
			throws NoSuchAlgorithmException, CertificateException, IOException,
			KeyStoreException {
		FileOutputStream fos = new FileOutputStream(file);
		KeyStore ks = KeyStore.getInstance("pkcs12");

		try {
			ks.load(null, password.toCharArray());
		} finally {
			ks.store(fos, password.toCharArray());
		}
		return ks;
	}

	private KeyStore loadKeyStore(File file, String password)
			throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException {
		FileInputStream fis = new FileInputStream(file);
		KeyStore ks = KeyStore.getInstance("pkcs12");
		ks.load(fis, password.toCharArray());
		return ks;
	}

	private void saveKeyStore(KeyStore ks, File file, String password)
			throws IOException, KeyStoreException, NoSuchAlgorithmException,
			CertificateException {
		java.io.FileOutputStream fos = null;
		try {
			fos = new java.io.FileOutputStream(file);
			ks.store(fos, password.toCharArray());
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	private X509Certificate convertBCtoJCECert(X509CertificateHolder holder)
			throws CertificateException {
		return new JcaX509CertificateConverter().setProvider("BC")
				.getCertificate(holder);
	}

	private KeyPair createJCEKeyPair() throws NoSuchAlgorithmException,
			NoSuchProviderException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
		generator.initialize(1024);
		return generator.generateKeyPair();
	}

	private PublicKey convertBctoJcePublic(AsymmetricKeyParameter key)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		RSAKeyParameters params = (RSAKeyParameters) key;
		RSAPublicKeySpec spec = new RSAPublicKeySpec(params.getModulus(),
				params.getExponent());
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}

	private PrivateKey convertBctoJcePrivate(AsymmetricKeyParameter key)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		RSAPrivateCrtKeyParameters params = (RSAPrivateCrtKeyParameters) key;

		RSAPrivateCrtKeySpec spec = new RSAPrivateCrtKeySpec(
				params.getModulus(), params.getPublicExponent(),
				params.getExponent(), params.getP(), params.getQ(),
				params.getDP(), params.getDQ(), params.getQInv());
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

	private AsymmetricKeyParameter convertJCEPublicToBC(PublicKey pubKey) {
		RSAPublicKey key = (RSAPublicKey) pubKey;
		return new RSAKeyParameters(false, key.getModulus(),
				key.getPublicExponent());
	}

	private AsymmetricKeyParameter convertJCEPrivateToBC(PrivateKey privateKey) {
		RSAPrivateCrtKey key = (RSAPrivateCrtKey) privateKey;
		return new RSAPrivateCrtKeyParameters(key.getModulus(),
				key.getPublicExponent(), key.getPrivateExponent(),
				key.getPrimeP(), key.getPrimeQ(), key.getPrimeExponentP(),
				key.getPrimeExponentQ(), key.getCrtCoefficient());
	}

	private AsymmetricCipherKeyPair createKeyPair()
			throws NoSuchAlgorithmException, NoSuchProviderException {
		RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
		generator.init(new RSAKeyGenerationParameters(new BigInteger("10001",
				16), SecureRandom.getInstance("SHA1PRNG", "SUN"), 1024, 80));
		AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();
		return keyPair;
	}

	public void main(String[] args) {
		AsymmetricCipherKeyPair masterKeys;
		try {
			/*
			 * 
			 * masterKeys = createKeyPair(); System.out.println( toPemString(
			 * convertBctoJcePublic(masterKeys.getPublic())) );
			 * System.out.println( toPemString(
			 * convertBctoJcePrivate(masterKeys.getPrivate())) );
			 * System.out.println("Master keys: " + masterKeys); String csr =
			 * createCSR(masterKeys); System.out.println("CSR: " + csr);
			 * X509CertificateHolder masterHolder =
			 * createCertificate(PZP_COMMON_NAME, masterKeys.getPrivate(),
			 * masterKeys.getPublic()); System.out.println( toPemString(
			 * masterHolder) );
			 * 
			 * X509Certificate masterCert = convertBCtoJCECert(masterHolder);
			 * System.out.println( toPemString( masterCert) );
			 */
			Security.addProvider(new BouncyCastleProvider());

			CertificateManager cc = new CertificateManager(keyStoreFile,
					"secret");
			KeyStore keyStore = cc.createEmptyKeyStore(keyStoreFile, "secret");
			cc.populateKeyStore(keyStore, "secret");

			cc.saveKeyStore(keyStore, keyStoreFile, "secret");

			System.out.println(keyStore.toString());
			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				System.out.println("Alias: " + alias);

			}

		} catch (IOException | CertificateManagerException | KeyStoreException
				| NoSuchAlgorithmException | CertificateException e) {
			e.printStackTrace();
		}
	}
	
	private X509Certificate loadX509FromPEMString(String certificate) throws CertificateException, IOException {
		PEMParser parser = new PEMParser(new StringReader(certificate));
		X509CertificateHolder holder = (X509CertificateHolder) parser.readObject();
		X509Certificate cert = convertBCtoJCECert(holder);
		return cert;
	}

	public static CertificateManager getInstance(Properties props) {
		if (instance == null) {
			instance = new CertificateManager(new File(
					props.getProperty("keystore.file")),
					props.getProperty("keystore.password"));
		}
		return instance;
	}

	public static CertificateManager getInstance() {
		if (instance == null) {
			return new CertificateManager();
		} else {
			return instance;
		}
	}
	
	public static CertificateManager getInstance(File file, String password) {
		if (instance == null) {
			instance = new CertificateManager(file, password);
		}
		return instance;
	}
	
	public boolean isPopulated() {
		return this.hasPopulatedKeyStore(this.keyStore);
	}

	public void populate() throws CertificateManagerException {
		if (isPopulated())
			throw new CertificateManagerException(
					"Key store not empty, cannot populate");
		if (!this.keyStoreFile.exists()) {
			try {
				this.keyStoreFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				throw new CertificateManagerException(
						"Could not create key store file", e);
			}
		}
		if (this.keyStore == null) {
			try {
				this.keyStore = createEmptyKeyStore(this.keyStoreFile, this.keyStoreSecret);
			} catch (NoSuchAlgorithmException | CertificateException
					| KeyStoreException | IOException e) {
				throw new CertificateManagerException(
						"Could not create empty key store", e);
			}
		}
		
		this.populateKeyStore(this.keyStore, this.keyStoreSecret);
	}

	public void save() throws CertificateManagerException {
		try {
			this.saveKeyStore(this.keyStore, this.keyStoreFile,
					this.keyStoreSecret);
		} catch (KeyStoreException | NoSuchAlgorithmException
				| CertificateException | IOException e) {
			throw new CertificateManagerException("Could not save key store", e);
		}
	}

	public File getKeyStoreFile() {
		return this.keyStoreFile;
	}

	public String getPzpMasterCSR() throws CertificateManagerException {
		assert(this.isPopulated());
		try {
			Certificate[] certs = keyStore.getCertificateChain("pzp-master");
			X509Certificate masterCert = (X509Certificate) certs[0];
			PrivateKey privKey = (PrivateKey) keyStore.getKey("pzp-master", this.keyStoreSecret.toCharArray());
			PublicKey pubKey = masterCert.getPublicKey();
			AsymmetricCipherKeyPair keyPair = new AsymmetricCipherKeyPair(convertJCEPublicToBC(pubKey), convertJCEPrivateToBC(privKey));
			return createCSR(keyPair);
		} catch (UnrecoverableKeyException | NoSuchAlgorithmException | OperatorCreationException | IOException | KeyStoreException e) {
			e.printStackTrace();
			throw new CertificateManagerException("Failed to generate CSR", e);
		}
		
	}
	
	public void load() throws CertificateManagerException {
		try {
			this.keyStore = loadKeyStore(this.keyStoreFile, this.keyStoreSecret);
		} catch (KeyStoreException | NoSuchAlgorithmException
				| CertificateException | IOException e) {
			e.printStackTrace();
			throw new CertificateManagerException("Could not load key store from file", e);
		}
	}
	
	public X509Certificate getCertificateFromString(String pemString) throws CertificateManagerException {
		try {
			return loadX509FromPEMString(pemString);
		} catch (CertificateException | IOException e) {
			throw new CertificateManagerException("Failed to convert PEM to X509 Certificate",e);
		}
	}

	
	public void addPzhCertificates(X509Certificate pzhMaster, X509Certificate pzpMaster) throws CertificateException {
		assert(this.isPopulated());
		try {
			this.addPZHCertificatesToStore(pzhMaster, pzpMaster);
		} catch (NoSuchAlgorithmException | UnrecoverableEntryException
				| KeyStoreException e) {
			throw new CertificateException("Failed to load additional certificates",e);
		}
	}
	
	public static boolean hasInstance() {
		return instance != null;
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}
}
