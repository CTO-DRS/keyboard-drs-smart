#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""DRS v1.16.0 — append the round's AR/EN string keys with full parity."""

AR = '''    <!-- DRS v1.16.0: شريط المهام الثابت (10 خانات) + محرر الخانة + لوحة الحركات كلوحة مفاتيح + الترتيب الذكي للوحات -->
    <string name="drs__strip_slot__title">تغيير المهمة — الخانة {slot}</string>
    <string name="drs__strip_slot__hint">اختر مهمة من الكتالوج لتشغل هذه الخانة في الشريط الثابت. لفتح هذا المحرر اضغط مطولًا على أي خانة في شريط المهام.</string>
    <string name="drs__strip_slot__pick">استبدال</string>
    <string name="drs__tools_drawer__level_section">المستوى:</string>
    <string name="drs__unified__bar_slots_hint">شريط المهام ثابت الآن: يعرض 10 مهام فقط فوق شريط الاقتراحات — تثبياتك أولًا ثم يُستكمل من الكتالوج. اضغط مطولًا على أي مهمة لتغييرها، وافتح الدرج (السحب الجانبي) لترتيبها وتثبيت غيرها.</string>
    <string name="pref__panels__smart_order__label">الترتيب الذكي للوحات</string>
    <string name="pref__panels__smart_order__summary">رقاقات مبدّل اللوحات تُرتَّب تلقائيًا: اللوحة الحالية أولًا ثم الأكثر فتحًا — محليًا فقط</string>
'''

EN = '''    <!-- DRS v1.16.0: the fixed tasks bar (10 slots) + the slot editor + the harakat keyboard panel + the smart panel ordering -->
    <string name="drs__strip_slot__title">Change task — slot {slot}</string>
    <string name="drs__strip_slot__hint">Pick a catalogue task to occupy this fixed-bar slot. Long-press any tasks-bar slot to open this editor.</string>
    <string name="drs__strip_slot__pick">Replace</string>
    <string name="drs__tools_drawer__level_section">Level:</string>
    <string name="drs__unified__bar_slots_hint">The tasks bar is now FIXED: it shows 10 tasks only above the suggestions strip — your pins first, then filled from the catalogue. Long-press any task to change it, and open the drawer (side-pull handle) to reorder or pin others.</string>
    <string name="pref__panels__smart_order__label">Smart panel ordering</string>
    <string name="pref__panels__smart_order__summary">The panels switcher reorders itself: the current panel leads, then the most-opened ones — local only</string>
'''

BASE = "app/src/main/res/values/strings.xml"
EN_PATH = "app/src/main/res/values-en/strings.xml"

for path, block in ((BASE, AR), (EN_PATH, EN)):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    assert content.rstrip().endswith("</resources>"), path
    content = content.replace("</resources>", block + "</resources>")
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"OK {path}")

import re
for path in (BASE, EN_PATH):
    with open(path, "r", encoding="utf-8") as f:
        n = len(re.findall(r"<string name=", f.read()))
    print(f"{path}: {n} keys")
