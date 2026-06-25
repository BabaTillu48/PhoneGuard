# PhoneGuard 🐱

एक simple Android app जो adult/blocked websites खोलने पर एक cat warning screen + आवाज़ दिखाता है, और सिर्फ phone restart करने पर हटता है। 30-day streak tracker भी शामिल है।

## ⚠️ ज़रूरी बात
यह app keyboard को track नहीं करता (वह privacy के लिए खतरनाक होता और Android इसकी इजाज़त भी नहीं देता)। इसके बदले यह आपके **browser की URL bar** को (Accessibility Service के ज़रिए) पढ़ता है — सिर्फ तब, जब browser खुला हो — और blocked keyword/domain मिलने पर warning दिखाता है। बाकी समय यह कुछ नहीं करता, इसलिए battery भी कम खाता है।

## 📦 Install कैसे करें (फ्री में, बिना Play Store)

### Option A — Android Studio से (recommended, सबसे आसान)
1. [Android Studio](https://developer.android.com/studio) download करके install करें (free)।
2. यह पूरा `PhoneGuard` folder अपने computer पर रखें।
3. Android Studio खोलें → **Open** → `PhoneGuard` folder select करें।
4. थोड़ी देर "Gradle Sync" चलेगा (पहली बार internet चाहिए होगा)।
5. अपना phone USB से connect करें, phone में **Developer Options → USB Debugging** ON करें।
6. ऊपर green ▶️ **Run** बटन दबाएं — app अपने phone में directly install और open हो जाएगी।

### Option B — सीधे APK बनाकर install करें
1. Android Studio में project खोलने के बाद: **Build → Build Bundle(s)/APK(s) → Build APK(s)**
2. बनी हुई APK file (`app/build/outputs/apk/debug/app-debug.apk`) को अपने phone में transfer करें (USB/Drive/WhatsApp)।
3. Phone पर file खोलें → "Install from unknown sources" allow करें → Install करें।

## 🛠️ App में पहली बार setup
App खोलने पर 2 permissions देनी होंगी:
1. **Accessibility Settings** → PhoneGuard को ON करें (यह browser की URL check करने के लिए ज़रूरी है)
2. **Overlay Permission** → allow करें (ताकि cat warning screen दिखा सके)

फिर "Blocked keywords" box में अपनी पसंद के words/domains डाल कर **Save Keywords** दबाएं। कुछ default keywords पहले से दिए गए हैं।

"Test करें" बटन से आप तुरंत देख सकते हैं cat screen कैसी दिखेगी।

## 🔄 कैसे काम करता है
- Browser में कोई blocked site/keyword detect होते ही → पूरी screen पर cat overlay + "मत कर लाला मत कर" आवाज़ (Hindi text-to-speech, हर 4 सेकंड में दोहराई जाती है)
- "बंद करें" बटन सिर्फ आवाज़ रोकता है — screen तब तक रहती है जब तक आप **phone restart** नहीं करते
- Restart करते ही screen हट जाती है और एक नई 30-day streak शुरू हो जाती है
- Home screen पर streak दिन गिनती दिखती है (X / 30 दिन)

## 🔋 Battery
Service सिर्फ तब active होता है जब कोई **browser app** खुला हो। बाकी सभी apps (games, social media, etc.) में यह कुछ नहीं करता, इसलिए battery पर असर बहुत कम पड़ता है।
