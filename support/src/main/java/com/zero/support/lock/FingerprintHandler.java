package com.zero.support.lock;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class FingerprintHandler
{
	private final Context context;
	private boolean errorStopped = true;
	
	private CallBack callback;
	
	private KeyStore keyStore;
	private final String KEY_NAME = "androidHive";
	private Cipher cipher;
	private final KeyguardManager keyguardManager;
	private final FingerprintManager fingerprintManager;

	private CancellationSignal cancellationSignal;

	@RequiresApi(api = Build.VERSION_CODES.M)
	public FingerprintHandler(Context mContext)
	{
		context = mContext;

		keyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
		fingerprintManager = (FingerprintManager) context.getSystemService(Activity.FINGERPRINT_SERVICE);
		
	}


	@RequiresApi(api = Build.VERSION_CODES.M)
	public boolean isReady()
	{
		if (!fingerprintManager.isHardwareDetected())
		{
			if(callback != null)
			callback.onInitializeError("Your Device does not have a Fingerprint Sensor");
		}
		else
		{
			if (context.checkSelfPermission( Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED)
			{
				if(callback != null)
				callback.onInitializeError("Fingerprint authentication permission not enabled");
			}
			else
			{
				if (!fingerprintManager.hasEnrolledFingerprints())
				{
					if(callback != null)
					callback.onInitializeError("Register at least one fingerprint in Settings");
				}
				else
				{
					if (!keyguardManager.isKeyguardSecure())
					{
						if(callback != null)
						callback.onInitializeError("Lock screen security not enabled in Settings");
					}
					else
					{
						generateKey();
						return cipherInit();
					}
				}
			}
		}
		return false;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	public void startScan()
	{
		if(isReady())
		{
			FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
			cancellationSignal = new CancellationSignal();
			fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, new CallbackInner(), null);
		}
	}

	public void stop()
	{
		errorStopped = false;
		cancellationSignal.cancel();
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void generateKey()
	{
		try
		{
			keyStore = KeyStore.getInstance("AndroidKeyStore");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}


		KeyGenerator keyGenerator;
		try
		{
			keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
		}
		catch (NoSuchAlgorithmException | NoSuchProviderException e)
		{
			throw new RuntimeException("Failed to get KeyGenerator instance", e);
		}


		try
		{
			keyStore.load(null);
			keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
							  .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
							  .setUserAuthenticationRequired(true)
							  .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
							  .build());
			keyGenerator.generateKey();
		}
		catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertificateException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}


	@TargetApi(Build.VERSION_CODES.M)
	private boolean cipherInit()
	{
		try
		{
			cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException e)
		{
			throw new RuntimeException("Failed to get Cipher", e);
		}


		try
		{
			keyStore.load(null);
			SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return true;
		}
		catch (KeyPermanentlyInvalidatedException e)
		{
			return false;
		}
		catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e)
		{
			throw new RuntimeException("Failed to init Cipher", e);
		}
	}

	
	
	@SuppressWarnings("deprecation")
	@RequiresApi(api = Build.VERSION_CODES.M)
	private class CallbackInner extends FingerprintManager.AuthenticationCallback
	{



		@Override
		public void onAuthenticationError(int errMsgId, CharSequence errString)
		{
			if(callback != null && errorStopped)
			callback.onAuthenticError(errString.toString());
		}


		@Override
		public void onAuthenticationHelp(int helpMsgId, CharSequence helpString)
		{
			if(callback != null)
			callback.onSensorReadyToScan();
		}


		@Override
		public void onAuthenticationFailed()
		{
			if(callback != null)
			callback.onAthenticNotMached();
		}


		@Override
		public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result)
		{
			if(callback != null)
			callback.onAuthenticMachedSucced();
		}
	}
	
	
	public static abstract class CallBack
	{
		public abstract void onInitializeError(String cause);
		
		public abstract void onAuthenticError(String cause);
		
		public abstract void onSensorReadyToScan();
		
		public abstract void onAthenticNotMached();
		
		public abstract void onAuthenticMachedSucced();
	}
	
	public void setCallback(CallBack callback)
	{
		this.callback = callback;
	}
}
