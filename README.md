# 🎨 CreateYourMeme

**CreateYourMeme** is a modern Android meme editor built using **Jetpack Compose**.  
It allows you to select or load a meme image, add customizable text boxes, move and resize them freely, then **save or share your memes** instantly — all in a clean Material 3 UI.

---

## 🚀 Features

✅ **Modern UI:** Built with Jetpack Compose and Material 3  
🖼️ **Meme Editing:** Add, drag, scale, and edit text boxes directly on the image  
💾 **Save & Share:** Save memes to your gallery or share instantly to any app  
📱 **Adaptive Layout:** Scales text and layout correctly across devices  
🌙 **Splash Screen Animation:** Polished startup with custom splash animation  
🔒 **Scoped Storage Support:** Fully compatible with Android 10+ storage policies  
⚡ **Lightweight:** No unnecessary libraries, small APK size

---

## 🧠 Tech Stack

| Component | Description |
|------------|--------------|
| **Language** | Kotlin |
| **UI Toolkit** | Jetpack Compose |
| **Architecture** | MVVM (ViewModel + StateFlow) |
| **Image Loading** | [Coil](https://github.com/coil-kt/coil) |
| **Persistence** | Android MediaStore + FileProvider |
| **Coroutines** | For async image loading and saving |
| **Splash Screen API** | Android 12+ SplashScreen (with animation) |

---

### 🧑‍💻 Clone this repository

```bash
git clone https://github.com/Shayar-Gupta/CreateYourMeme.git
cd CreateYourMeme

## 🧩 App Structure

