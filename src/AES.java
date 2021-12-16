import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class AES {

		private SecretKey key;
		private byte[] iv = new byte[16];

		public void generateKey(BigInteger diffieHellman_key) {

			char[] password = diffieHellman_key.toString().toCharArray();
			SecretKeyFactory factory;
			try {
				factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				KeySpec spec = new PBEKeySpec(password, new byte[8], 65536, 256);
				SecretKey tmp = factory.generateSecret(spec);
				this.key = new SecretKeySpec(tmp.getEncoded(), "AES");
			} catch (NoSuchAlgorithmException | InvalidKeySpecException error) { error.printStackTrace(); }
			
		}
		
		public String encrypt(String decrypted_message) {
			try {
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(this.iv));
				byte[] ciphertext = cipher.doFinal(decrypted_message.getBytes(StandardCharsets.UTF_8));
				String encrypted_message = Base64.getEncoder().encodeToString(ciphertext);
				return encrypted_message;
			} catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
				e.printStackTrace();
				return null;
			}
		}


		public String decrypt(String encrypted_message) {
			try {
				byte[] ciphertext = Base64.getDecoder().decode(encrypted_message);
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, this.key, new IvParameterSpec(this.iv));
				String decrypted_message = new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
				return decrypted_message;
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		public String getKey() {
			return Base64.getEncoder().encodeToString(key.getEncoded());
		}
		
	}
