# Firebase kurulumu — Hesap ve bulut yedeği

Uygulama `app/google-services.json` dosyası olmadan da derlenir ve çalışır; o durumda hesap özellikleri gizlenir ve herkes misafirdir. Hesapları açmak için:

## 1. Firebase projesi
1. https://console.firebase.google.com → **Proje ekle** → ad: `BenimGunlerim` (Google Analytics gerekmez).
2. **Android uygulaması ekle**:
   - Paket adı: `com.benimgunlerim` (yayındaki sürüm)
   - Test sürümü için ikinci bir Android uygulaması: `com.benimgunlerim.debug`
3. İndirilen `google-services.json` dosyasını `app/` klasörüne koyun. (İki uygulama da aynı dosyada yer alır.)

## 2. Authentication
- **Build › Authentication › Get started › Sign-in method › Email/Password → Enable** (Email link kapalı kalabilir).
- **Templates** sekmesinde doğrulama ve şifre sıfırlama e-postalarının dilini **Türkçe** yapın.

## 3. Cloud Firestore
1. **Build › Firestore Database › Create database** → Konum: `eur3 (europe-west)` → **Production mode**.
2. **Rules** sekmesine depodaki `firestore.rules` dosyasının içeriğini yapıştırıp **Publish**.

## 4. Güncelleme pop-up'ı (Remote Config)
Anahtarlar: `latest_version_code` (sayı), `min_version_code` (sayı), isteğe bağlı `update_message` (metin), `update_store_url` (metin).
- **Yeni sürüm yayınlandığında:** Play Console'da sürüm *onaylanıp yayına girdikten sonra* `latest_version_code`'u yeni `versionCode`'a çekip **Publish**. Eski sürümdekiler açılışta "Yeni sürüm hazır" görür (Sonra → 24 saat sorulmaz).
- **Zorunlu güncelleme:** yalnızca `min_version_code` desteklenen en eski `versionCode`'a çekilirse. 0 iken kapalı.
- Test sürümünde canlı ayara dokunmadan: `adb shell am start -n com.benimgunlerim.debug/com.benimgunlerim.MainActivity --es debug_update_prompt optional` (ya da `required`).

## 5. Duyuru bildirimleri (Cloud Messaging)
Console › Messaging › yeni kampanya › Firebase Notification messages › Hedef: **Topic**.
- `debug` → yalnızca test sürümü yüklü telefonlar (önce burada dene).
- `all` → yayındaki herkes (misafirler dahil).
- İsteğe bağlı özel veri: `route` = `plan` / `routines` → bildirime dokununca o sekme açılır (uygulama açıkken gelen mesajlarda).

## 6. Play Console
- **Uygulama içeriği › Veri güvenliği** formunu güncelleyin:
  - Toplanan: *E-posta adresi* (hesap yönetimi), *Uygulama etkinliği / kullanıcı içeriği* (görev, rutin — uygulama işlevi, yedekleme).
  - Aktarımda şifreli: **Evet**. Kullanıcı silme isteyebilir: **Evet** (uygulama içi “Hesabı sil”).
- **Hesap silme** alanına: uygulama içi yol (*Ayarlar › Hesap › Hesabı sil*) ve web üzerinden talep için iletişim e-postası.
- Web’deki gizlilik politikası sayfasını uygulama içindeki metinle aynı hale getirin.

## Nasıl çalışır
- Telefon her zaman çalışan kopyadır; internet olmadan tam çalışır.
- Bulutta `users/{uid}` belgesinde, “Verileri dışa aktar” ile aynı JSON gzip’lenmiş olarak durur.
- Açılışta başka bir cihaz daha yeni yedek yazdıysa indirilir; arka plana geçerken yedeklenir; Ayarlar’da “Şimdi yedekle”.
- Misafirken hesap açılırsa telefondaki veri hesaba taşınır. Hem telefonda hem hesapta veri varsa kullanıcıya sorulur.
- Sınır: iki cihaz aynı anda, aradaki eşitlemeden önce değişiklik yaparsa son yazan kazanır.
