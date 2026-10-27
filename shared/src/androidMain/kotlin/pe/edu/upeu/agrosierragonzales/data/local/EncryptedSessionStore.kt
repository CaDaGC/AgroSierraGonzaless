package pe.edu.upeu.agrosierragonzales.data.local
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import org.json.*
import pe.edu.upeu.agrosierragonzales.data.repository.SecureSessionStore
import pe.edu.upeu.agrosierragonzales.domain.model.Sesion
class EncryptedSessionStore(c:Context):SecureSessionStore{
 private val prefs=c.getSharedPreferences("sesion_cifrada",0);private val alias="agrosierra_token"
 private fun key():SecretKey{val ks=KeyStore.getInstance("AndroidKeyStore").apply{load(null)};(ks.getKey(alias,null)as?SecretKey)?.let{return it};return KeyGenerator.getInstance("AES","AndroidKeyStore").apply{init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())}.generateKey()}
 override fun write(x:Sesion){val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());val j=JSONObject().put("t",x.token).put("u",x.usuario).put("n",x.nombre).put("r",x.rol).put("p",JSONArray(x.privilegios.toList())).toString().toByteArray();prefs.edit().putString("s",Base64.encodeToString(c.iv,0)+":"+Base64.encodeToString(c.doFinal(j),0)).apply()}
 override fun read():Sesion?=runCatching{val p=prefs.getString("s",null)?.split(":")?:return null;val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,Base64.decode(p[0],0)));val j=JSONObject(String(c.doFinal(Base64.decode(p[1],0))));val a=j.getJSONArray("p");Sesion(j.getString("t"),j.getString("u"),j.getString("n"),j.getString("r"),(0 until a.length()).map{a.getString(it)}.toSet())}.getOrNull()
 override fun clear(){prefs.edit().remove("s").apply()}
}
