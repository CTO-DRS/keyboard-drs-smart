<div align="center">

# DRS Smart Keyboard V 1.19.0

## الجولة الشاملة التاسعة عشرة — الفحص الشامل: المدقق الإملائي الحقيقي، البحث في الإيموجي يعمل أخيرًا، تقسيم/دمج اللوحة بضغطة، صور مصغرة بلا تجميد، وإحياء قنوات ميتة حقيقية

**Nineteenth Comprehensive Round — The Full Audit: Real Spell Checking, Working Emoji Search, One-Tap Split/Merge, Jank-Free Thumbnails, and Genuine Dead Channels Revived**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.19.0 — جولة الفحص الشامل

هذه الجولة بدأت بفحص شامل ثلاثي الجبهات (محرك الإدخال واللوحات، طبقة
الإيموجي والحافظة واللوحات الذكية، وطبقة التطبيق والإعدادات والقواميس)
قارن التطبيق بمعايير أقوى لوحات المفاتيح عالميًا. النتيجة: قنوات ميتة
حقيقية أُحييت كلها، وميزتان عالميتان ناقصتان وُصلتا فعليًا.

- ✅ **المدقق الإملائي الحقيقي — أخيرًا** — خدمة المدقق كانت موصولة
  بالكامل (جلسات، ~180 لغة معلنة، شاشة تشخيص) لكن دالة `spell()` نفسها
  كانت تعيد «كلمة صحيحة» دائمًا بلا استثناء: **لا خط أحمر واحد منذ
  اليوم الأول**. الآن تُحدَّد الأخطاء فعليًا بخط أحمر مع اقتراحات التصحيح
  عند اللمس، خلف **فاصل قراري نقي محافظ** (SpellingDecider): الحكم فقط
  على قواميس غنية (العربية 50 ألف كلمة والإنجليزية 50 ألف — القواميس
  الثماني المختصرة لا تحكم أصلًا لأنها ستضع خطًا تحت نصف الجملة)، كلمات
  نقية بلا أرقام أو رموز، الاختصارات الكبيرة (DRS وNASA) محمية، والأسماء
  العلمية في منتصف الجملة محمية، ولا يوضع خط أبدًا إلا إذا وُجد تصحيح
  معقول على مسافة تحرير واحدة فعلًا. ولأن «المجهول لا ينذر كاذبًا» بقيت
  القاعدة الحاكمة، أُضيف مفتاح **«تحديد الأخطاء الإملائية»** في شاشة
  الكتابة لإيقاف القناة كلها بضغطة. المدقق يُصغي أيضًا لقاموسك الشخصي
  والكلمات المتعلمة فلا يعاقب ما علّمتَه إياه بنفسك.

- 🔍 **البحث في الإيموجي يعمل أخيرًا — بسبع لغات وبكلماتك** —
  اكتشف الفحص أن لوحة الإيموجي كانت تحمّل ملف `root.txt` الذي أعمدة
  أسمائه وكلماته المفتاحية **فارغة في 3,944 من 3,965 سطرًا**، فيما كانت
  ملفات التعريفات الكاملة (CLDR v48: العربية والإنجليزية والألمانية
  والإسبانية والفرنسية والإيطالية والبرتغالية) ترمق في الأصول دون
  استهلاك من اللوحة! البحث كان «لا نتائج» دائمًا. الآن تتبع اللوحة لغة
  النظام النشط: ابحث «قلب» في العربية و«Herz» في الألمانية، واللغات
  بلا ملف تعريفات تتراجع للإنجليزية تراجعًا صادقًا (وإلا لملف البنية
  الأصلي) — وبذلك انبعثت اقتراحات الإيموجي في المحرك نفسه للغات كانت
  ميّتة هناك أيضًا. **والسطر المشؤوم تحميل اللوحة أصبح مرتبطًا بلغة
  النظام النشط فتتبدل مع تغيير اللغة.**

- ⌨️ **تقسيم/دمج لوحة المفاتيح بضغطة** — التقسيم في v1.18.0 كان
  حكرًا على شاشة الإعدادات. رمزاه المعرّفان منذ الإصدار السابق كانا
  بلا معالج ولا ظهور (قناة ميتة نمطية): الآن **«تقسيم اللوحة»** و**«دمج
  اللوحة»** أداتا كتالوج كاملتان (49 أداة) بأيقونتين واسم ووصف ومفتاح
  إحصاء، تقلبان نفس مفتاح المحرك الذي تقلبه الإعدادات مع توست تأكيد،
  ملحقتان بذيل الكتالوج حفاظًا على ترتيبك وتثبيتك المحفوظين.

- 🖼️ **صور مصغرة بلا تجميد — خارج الخيط الرئيسي** — فك ترميز صور
  وفيديوهات الحافظة كان يجري **بشكل متزامن داخل التركيب** على خيط
  الواجهة (`remember{}`): صورة كبيرة = لوحة مفاتيح متجمدة مئات
  الميلي ثانية. انتقل الفك إلى `Dispatchers.IO` مع **كاش LRU مقيد
  بـ48 مدخلًا** فتمرير سجل الوسائط صار سلسًا، مع رسالة فشل معرّبة
  بدل النص الإنجليزي المضمّن.

- 📊 **توازي إحصائي: أداتا v1.8.0 تُحسبان أخيرًا** — «فصل الأرقام عن
  الحروف» و«إزالة الترقيم» كانتا تعملان عملهما النقي لكن ضغطتَيهما لم
  تُحتسبا قط في «الأكثر استخدامًا» (لم تكونا في سجل SmartToolCodes
  ولا في ثوابت KeyCode). صارتا مُحتسبتين بأسماء ثابتة صريحة، وتظهر
  بلاطاتهن في الأكثر استخدامًا مثل باقي الأربع والثلاثين.

- 🧹 **قنوات ميتة حقيقية أُحييت — ثمرة الفحص** —
  **`NlpManager.destroyIfNecessary`**: الشرط كان `getAndSet(true)`
  فيُدمر المزود في كل استدعاء ويترك الراية حية إلى الأبد فلا يُعاد
  إنشاؤه مرة أخرى أبدًا — صار يُدمَّر فقط وهو حي ويعود للحياة صحيحًا.
  **`UserDictionaryDatabase.reset()`**: أصلان من `TODO("Not yet
  implemented")` — أي مستدعٍ مستقبلي كان سينهار — صارتا مسحًا حقيقيًا
  (قاعدة Room والنظام) خلف زر **«مسح القاموس»** بحوار تأكيد في شاشة
  قاموس المستخدم الداخلي. **`EmojiHistoryPopup`**: شرطا السهمين كانا
  منسوخين متطابقين فظهرا عند الحواف حيث التحريك بلا أثر،
  و`numActions = 1` مجازفة ثابتة تجعل النافذة تطفو عاليًا — صار
  السهمان واعيين بالموضع (لا سهم عند الحافة) والنافذة تُحسب من
  الأفعال المعروضة فعلًا، وبوابة الفرز اليدوي امتزجت للقائمة الصحيحة
  (المثبت بقاعدته، والأخير بقاعدته).

- 🌐 **مسح i18n: أحد عشر نصًا مضمنًا صارت سلاسل** — رقائق «نص/صور/فيديو»
  في لوحة الحافظة (كانت إنجليزية جامدة)، توست «فشل لصق العنصر» في
  مسارين، تلميح «إدراج النص» في محرر الأفعال السريعة، وصف العنصر الفاشل
  للوسائط، ستة أوصاف وصولية في نافذة محرر الحافظة (إغلاق/مشاركة/حفظ
  كملف/السابق/التالي/مسح البحث)، «Show subtype picker» المتسربة إنجليزيًا
  في قائمة إعدادات الإيماءات، و«بسيط/تقني/مزدوج» المضمّنة عربيًا في
  شريط النظام الموحد — كلها مفاتيح سلاسل معرّبة بالعربية والإنجليزية.
  **28 مفتاحًا جديدًا — توازي 2,376 لكل لغة.**

- 🧪 **15 اختبار وحدة جديدًا (472 ناجحًا، كانت 457)** — فاصل القرار
  الإملائي بعقوده السبعة (الطول، الأرقام والرموز، الاختصارات، الأسماء
  العلمية، القواميس الرقيقة، الحاجة لتصحيح معقول، القبول الكامل)، سلسلة
  دقة مسارات الإيموجي (الأولوية لغة←بلد←تنويع، الرجوع للإنجليزية ثم
  للبنية، الحساسية للحالة)، أداتا التقسيم/الدمج من الرمز إلى الكتالوج،
  وتوازي الأداتين الإحصائيتين — مع تحديث عقود ذيل الكتالوج في اختبارات
  الجولات السابقة (47 أداة ← 49).

## التثبيت

1. حمّل ملف `DRS-Smart-Keyboard-v1.19.0.apk` من الأسفل.
2. ثبّته (اسمح بالتثبيت من مصادر غير معروفة عند الحاجة).
3. افتح الإعدادات ← أنظمة ← اللغات وأدخل لوحة المفاتيح ← فعّلها.
4. اخترها لوحة مفاتيح افتراضية وابدأ الكتابة.

تحقق من سلامة الملف بمجموعات SHA-256 في `SHA256SUMS.txt` المرفق.

## الخصوصية

كل شيء يعمل **محليًا وبلا اتصال**: لا حسابات، لا تتبع، لا إعلانات، ولا
شبكة إلا فحص تحديث التطبيق نفسه. اقرأ `PRIVACY.md` كاملة.

</div>

---

<div dir="ltr">

## About the project

**DRS Smart Keyboard** is a free, open-source Android keyboard
(Apache-2.0) built with Kotlin, Jetpack Compose and Material 3 —
**Arabic-first**: a fully Arabic interface with native RTL support,
thoughtful Arabic layouts and Arabic suggestions/correction, with English
as a complete second option. Everything stays on your device: no accounts,
no tracking, no ads.

## What's new in V 1.19.0 — The Full Audit Round

This round began with a three-front deep audit (input engine, panels,
app/settings layer) benchmarked against the world's strongest keyboards.
Every genuine dead channel found was revived, and two world-class
missing capabilities were actually wired.

- ✅ **Real spell checking — at last** — the spell-checker service was
  fully wired (sessions, ~180 advertised locales, a diagnostics screen),
  yet `spell()` itself unconditionally returned "valid word": **not a
  single red underline since day one**. Misspellings are now flagged in
  red with tap-to-fix suggestions, behind a **conservative pure decision
  engine** (SpellingDecider): only rich dictionaries judge (Arabic 50k
  and English 50k — the eight thin curated v1.18.0 dictionaries never
  flag, or they'd underline half a sentence), letters-only words, all-caps
  acronyms protected (DRS, NASA), mid-sentence capitalized proper nouns
  protected, and a word is only flagged when a plausible edit-distance-1
  correction actually exists. Because "the unknown never warns falsely"
  remains the governing rule, a **"Flag spelling mistakes"** switch in the
  Typing screen turns the whole channel off. The checker also respects
  your personal and learned words.

- 🔍 **Emoji search finally works — in seven languages, your words** —
  the audit found the palette loaded `root.txt` whose name/keyword columns
  are **empty for 3,944 of 3,965 lines**, while the full CLDR v48
  annotation files (Arabic, English, German, Spanish, French, Italian,
  Portuguese) sat unused in assets. Search was permanently "no results".
  The palette now follows the active subtype locale: search "قلب" in
  Arabic, "Herz" in German; languages without annotations fall back to
  English honestly (then to the structural root file) — which also
  revived the engine's emoji suggestions for languages that were dead
  there too. **The palette reloads with the active subtype.**

- ⌨️ **One-tap split/merge keyboard** — splitting was settings-only in
  v1.18.0; its two defined codes had no handler and no exposure (a
  textbook dead channel). **"Split keyboard"** and **"Merge keyboard"**
  are now full catalogue tools (49 tools) with icons, names, descriptions
  and stats registration, driving the same engine pref the settings
  screen drives, with a status toast, appended at the catalogue tail so
  your saved order and pins keep their meaning.

- 🖼️ **Jank-free thumbnails — off the main thread** — clipboard image and
  video decoding ran **synchronously inside composition** on the UI
  thread (`remember{}`): a large photo froze the whole keyboard for
  hundreds of milliseconds. Decoding moved to `Dispatchers.IO` with a
  **bounded 48-entry LRU cache** so scrolling the media history is smooth,
  with a localized failure message replacing the hardcoded English text.

- 📊 **Stats parity: the two v1.8.0 tools finally count** — "separate
  digits from letters" and "remove punctuation" performed their pure
  work, but their presses were never counted in the most-used surface
  (absent from SmartToolCodes and the KeyCode constants). Both are now
  counted under explicit named constants, surfacing as most-used tiles
  like the other thirty-four.

- 🧹 **Real dead channels revived — the audit's fruit** —
  **`NlpManager.destroyIfNecessary`**: the condition used `getAndSet(true)`,
  destroying the provider on EVERY call and leaving the alive-flag set
  forever so it could never be re-created — it now destroys only when
  alive and marks the instance dead. **`UserDictionaryDatabase.reset()`**:
  two `TODO("Not yet implemented")` landmines any future caller would
  have crashed on — now real wipes (Room database and the system
  provider) behind a **"Clear dictionary"** action with a confirmation
  dialog in the internal user-dictionary screen. **`EmojiHistoryPopup`**:
  the move-arrow conditions were copy-paste identical, rendering arrows
  at list edges where moving is a no-op, and `numActions = 1` was a
  hardcoded guess floating the popup too high — arrows are now
  position-aware (none at an edge), the offset counts the actions actually
  shown, and the manual-sort gate tests the correct list's strategy.

- 🌐 **i18n sweep: eleven hardcoded literals became strings** — the
  clipboard "Text/Images/Videos" chips (hardwired English), the "Failed
  to paste item." toast in two paths, the quick-actions "Insert text"
  tooltip, the media-tile unknown-error text, six accessibility
  descriptions in the clip editor popup (close/share/save as file/prev/
  next/clear find), the English "Show subtype picker" leaking into the
  gesture-settings enum, and the Arabic "بسيط/تقني/مزدوج" literals in
  the unified strip — all localized in both languages. **28 new keys —
  exact 2,376-key per-language parity.**

- 🧪 **15 new unit tests (472 passing, up from 457)** — the spelling
  decision engine's seven contracts (length, digits/symbols, acronyms,
  proper nouns, thin dictionaries, correction requirement, full
  acceptance), the emoji asset-path chain (language→country→variant
  priority, honest en/root fallbacks, case-insensitivity), the
  split/merge tools from code to catalogue, and the two stats-parity
  codes — with the previous rounds' catalogue tail contracts updated
  (47 → 49 tools).

## Install

1. Download `DRS-Smart-Keyboard-v1.19.0.apk` below.
2. Install it (allow unknown sources when prompted).
3. Open Settings → System → Languages & input → enable the keyboard.
4. Pick it as your default keyboard and start typing.

Verify integrity with the SHA-256 checksums in the attached `SHA256SUMS.txt`.

## Privacy

Everything runs **locally and offline**: no accounts, no tracking, no ads,
no network except the app's own update check. Read the full `PRIVACY.md`.

</div>
