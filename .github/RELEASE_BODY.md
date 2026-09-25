<div align="center">

# DRS Smart Keyboard V 1.13.0

## الجولة الشاملة الثالثة عشرة — القفز المباشر إلى السطر: اضغط بطاقة النتيجة فينتقل المحرر مباشرة إلى سطرها

**Thirteenth Comprehensive Round — The Direct Line Jump: Tap a Result Card and the Editor Flies Straight to Its Line**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.13.0 — القفز المباشر إلى السطر

- 🎯 **اضغط البطاقة فينتقل مباشرة إلى السطر** (طلب المستخدم الصريح:
  «اضف عن بطاقات النتائج الملونة يظغط على البطاقه فينتقل مباشرة،الى
  السطر»): الضغط على أي بطاقة نتيجة ملونة — في النافذة المنبثقة والمحرر
  داخل اللوحة معًا — **يمرر حقل المحرر مباشرة إلى سطر المطابقة** بحركة
  انسيابية، فالمطابقة تظهر أمامك فورًا مهما كان النص طويلًا، حتى
  500,000 حرف. لم يعد القفز مجرد تفعيل العدّاد والتوهج — الآن الحقل
  نفسه يطير إلى السطر.
- 🧭 **أسهم التنقل تقفز أيضًا**: السابق/التالي في شريط البحث لم يعودا
  يحدّان العدّاد فقط — كل ضغطة تنقل الحقل إلى سطر المطابقة التالية أو
  السابقة، فيصبح تجول النتائج رحلة مرئية كاملة: عدّاد «{active} من
  {count}» يتحدث، البطاقة النشطة تتوهج، والمطابقة تتمركز أمام عينيك.
- 📐 **دقة هندسية من التخطيط الحقيقي للنص**: موضع القفز يُحسب من
  تخطيط النص الفعلي للحقل — **بالأسطر المرئية الملفوفة لا بالأسطر
  المنطقية** — فالسطر الطويل الملتف على عدة صفوف يُعامَل كما يُرى
  على الشاشة. السطر الملائم **يتمركز في منتصف نافذة العرض**، وإن
  كان أطول من النافذة نفسها **يُثبَّت رأسه تحت الحافة العليا** —
  والنتيجة تُقيَّد دائمًا بمدى التمرير الحقيقي.
- ↔️ **قائمة البطاقات تتبع النشط في الاتجاهين**: اضغط بطاقة أو تنقل
  بالأسهم، فتتمرر قائمة النتائج ذاتها تلقائيًا حتى تبقى البطاقة
  النشطة مرئية — وبعد كل استبدال تعود القائمة إلى المطابقة النشطة.
  القفز طريق ذو اتجاهين بين البطاقات والنص.
- 🛡️ **محرك نقي مُختبر بالكامل**: حساب القفز عقدٌ نقوي في
  `ClipResultJump` (التوسيط، التثبيت، القيدان السفلي والعلوي،
  الحالات الدفاعية الصادقة: لا تخطيط بعد → لا قفز، إزاحة خارج
  النص → لا قفز، نافذة غير معروفة → تثبيت الرأس) — وكل سلوك
  مثبت ضد تخطيطات وهمية دقيقة قبل أن يلمس الواجهة.

## ماذا يعني هذا عمليًا؟

انسخ نصًا من 200 سطر → افتح «تعديل النص» → ابحث عن كلمة تظهر في
المطابقة الثلاثين → اضغط بطاقتها الملونة: الحقل ينزلق مباشرة إلى
سطرها ويتمركز أمامك. اضغط السهم للأسفل عشر مرات: تقفز من مطابقة
إلى مطابقة والعديد يتحرك والتوهج يلاحقك. هذه هي قائمة نتائج تتصرف
كما تتخيلها — بلا بحث بصري بعد اليوم.

<div dir="ltr">

## What's new in V 1.13.0 — The Direct Line Jump

- 🎯 **Tap the card, fly to its line**: tapping any colored result
  card — in the popup window and the in-panel editor alike — scrolls
  the editor field straight to the match's row with a smooth
  animation, at any length up to 500,000 characters.
- 🧭 **The navigation arrows jump too**: prev/next now move the field
  to the next or previous match's line, making result-walking a full
  visual journey — counter, glow, and viewport move together.
- 📐 **Geometry from the real text layout**: the jump target is
  computed from the field's actual text layout — wrapped visual rows,
  not logical lines. A fitting row is centered in the viewport; a row
  taller than the viewport is pinned just under the top edge; the
  result is always clamped to the real scroll range.
- ↔️ **The cards list tracks the active card both ways**: tap a card
  or use the arrows and the results list auto-scrolls to keep the
  active card visible — and returns to the active match after every
  replacement.
- 🛡️ **A pure, fully-tested engine**: the jump math is pinned in
  `ClipResultJump` (centering, pinning, both clamps, honest defensive
  nulls) and verified against tiny fake layouts before it touches the
  UI.

</div>

## التحميل

| الأصل | الوصف |
|---|---|
| `DRS-Smart-Keyboard-v1.13.0.apk` | الحزمة الكاملة الموقّعة (versionCode 25) |
| `DRS-Smart-Keyboard-v1.13.0.aab` | حزمة أندرويد للمتاجر |
| `SHA256SUMS.txt` | بصمات SHA-256 لكل الأصول |

</div>
