package com.xio.xcodestudio;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class MainActivity extends Activity {
    private LinearLayout root, content, topBar;
    private String theme = "black";
    private String language = "English";
    private File projectsDir;
    private static final int PICK_ZIP = 41;

    private int bg() {
        if ("white".equals(theme)) return Color.rgb(245,245,245);
        if ("comfort".equals(theme)) return Color.rgb(32,34,39);
        return Color.BLACK;
    }
    private int panel() {
        if ("white".equals(theme)) return Color.rgb(232,234,237);
        if ("comfort".equals(theme)) return Color.rgb(47,50,57);
        return Color.rgb(25,25,25);
    }
    private int fg() { return "white".equals(theme) ? Color.rgb(25,25,25) : Color.WHITE; }
    private int muted() { return "white".equals(theme) ? Color.DKGRAY : Color.LTGRAY; }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg());
        getWindow().setNavigationBarColor(bg());
        projectsDir = new File(getFilesDir(), "projects");
        if (!projectsDir.exists()) projectsDir.mkdirs();
        theme = getPreferences(0).getString("theme", "black");
        language = getPreferences(0).getString("language", "English");
        showHome();
    }

    private String tr(String en, String ar) {
        return "العربية".equals(language) ? ar : en;
    }

    private void shell(String title) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg());
        root.setPadding(dp(16), dp(12), dp(16), dp(12));
        setContentView(root);

        topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text(title, 21, true);
        topBar.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        Button settings = button("⚙");
        settings.setOnClickListener(v -> showSettings());
        topBar.addView(settings);
        root.addView(topBar);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(18), 0, 0);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private void showHome() {
        shell("XCode Studio");
        TextView heading = text(tr("Your workspace", "مساحة العمل"), 25, true);
        content.addView(heading);
        content.addView(text(tr("Android development workspace", "بيئة تطوير أندرويد"), 14, false));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button create = button(tr("+ New project", "+ مشروع جديد"));
        create.setOnClickListener(v -> createProjectDialog());
        Button importZip = button(tr("Import ZIP", "استيراد ZIP"));
        importZip.setOnClickListener(v -> pickZip());
        actions.addView(create, new LinearLayout.LayoutParams(0, dp(52), 1));
        actions.addView(importZip, new LinearLayout.LayoutParams(0, dp(52), 1));
        content.addView(actions);
        addSection(tr("Projects", "المشاريع"));
        File[] dirs = projectsDir.listFiles(File::isDirectory);
        if (dirs == null || dirs.length == 0) {
            content.addView(cardText(tr("No projects yet. Create a project or import a ZIP archive.",
                    "لا توجد مشاريع بعد. أنشئ مشروعًا أو استورد ملف ZIP.")));
        } else {
            Arrays.sort(dirs, (a,b) -> Long.compare(b.lastModified(), a.lastModified()));
            for (File dir : dirs) addProjectCard(dir);
        }
        addSection(tr("Developer tools", "أدوات التطوير"));
        addNavCard("GitHub", tr("Account connection and repositories", "ربط الحساب والمستودعات"), () -> showGitHub());
        addNavCard("Build & Security", tr("Build workflow and security checks", "التجميع وفحوصات الأمان"), () -> showBuild());
        addNavCard("AI Providers", tr("Optional API provider settings", "إعدادات مزودي الذكاء الاصطناعي الاختيارية"), () -> showAI());
    }

    private void createProjectDialog() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(tr("Project name", "اسم المشروع"));
        new AlertDialog.Builder(this).setTitle(tr("New project", "مشروع جديد"))
            .setView(input)
            .setNegativeButton(tr("Cancel", "إلغاء"), null)
            .setPositiveButton(tr("Create", "إنشاء"), (d,w) -> {
                String name = safeName(input.getText().toString().trim());
                if (name.isEmpty()) name = "NewProject";
                File dir = new File(projectsDir, name);
                if (dir.exists()) { toast(tr("A project with this name already exists.", "يوجد مشروع بهذا الاسم.")); return; }
                dir.mkdirs();
                try {
                    write(new File(dir, "README.md"), "# " + name + "\n\nCreated with XCode Studio.\n");
                    write(new File(dir, "settings.json"), "{\n  \"name\": \"" + name + "\",\n  \"type\": \"Android\"\n}\n");
                } catch(Exception e) { toast(e.getMessage()); }
                showHome();
            }).show();
    }

    private void pickZip() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("application/zip");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/zip","application/x-zip-compressed","application/octet-stream"});
        startActivityForResult(i, PICK_ZIP);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ZIP && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                String name = "Imported_" + System.currentTimeMillis();
                File out = new File(projectsDir, name);
                out.mkdirs();
                int count = unzipSafely(uri, out);
                toast(tr("Imported files: ", "تم استيراد الملفات: ") + count);
                showHome();
            } catch(Exception e) {
                toast(tr("ZIP import failed: ", "فشل استيراد ZIP: ") + e.getMessage());
            }
        }
    }

    private int unzipSafely(Uri uri, File destination) throws Exception {
        int count = 0;
        try (InputStream in = getContentResolver().openInputStream(uri);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File target = new File(destination, entry.getName());
                String rootPath = destination.getCanonicalPath() + File.separator;
                if (!target.getCanonicalPath().startsWith(rootPath)) throw new SecurityException("Unsafe ZIP path");
                if (entry.isDirectory()) target.mkdirs();
                else {
                    File parent = target.getParentFile();
                    if (parent != null) parent.mkdirs();
                    try (OutputStream out = new FileOutputStream(target)) {
                        byte[] buffer = new byte[8192]; int n;
                        while ((n = zis.read(buffer)) > 0) out.write(buffer, 0, n);
                    }
                    count++;
                }
                zis.closeEntry();
            }
        }
        return count;
    }

    private void addProjectCard(File dir) {
        LinearLayout card = card();
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.addView(text(dir.getName(), 17, true));
        info.addView(text(dir.listFiles() == null ? "0 items" : dir.listFiles().length + " items", 12, false));
        row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        Button open = button(tr("Open", "فتح"));
        open.setOnClickListener(v -> showProject(dir));
        Button more = button("⋮");
        more.setOnClickListener(v -> projectMenu(dir));
        row.addView(open); row.addView(more);
        card.addView(row);
        content.addView(card);
    }

    private void projectMenu(File dir) {
        String[] items = {tr("Rename", "إعادة تسمية"), tr("Export ZIP", "تصدير ZIP"), tr("Delete", "حذف")};
        new AlertDialog.Builder(this).setTitle(dir.getName()).setItems(items, (d, which) -> {
            if (which == 0) renameProject(dir);
            if (which == 1) toast(tr("ZIP export is planned for the next implementation step.", "تصدير ZIP سيضاف في مرحلة التنفيذ التالية."));
            if (which == 2) new AlertDialog.Builder(this).setTitle(tr("Delete project?", "حذف المشروع؟"))
                .setMessage(tr("This permanently deletes the local project files.", "سيحذف ملفات المشروع المحلية نهائيًا."))
                .setNegativeButton(tr("Cancel", "إلغاء"), null)
                .setPositiveButton(tr("Delete", "حذف"), (x,y) -> { deleteTree(dir); showHome(); }).show();
        }).show();
    }

    private void renameProject(File dir) {
        EditText input = new EditText(this); input.setText(dir.getName());
        new AlertDialog.Builder(this).setTitle(tr("Rename project", "إعادة تسمية المشروع")).setView(input)
            .setNegativeButton(tr("Cancel", "إلغاء"), null).setPositiveButton(tr("Save", "حفظ"), (d,w) -> {
                String n = safeName(input.getText().toString().trim());
                if (!n.isEmpty()) dir.renameTo(new File(projectsDir, n));
                showHome();
            }).show();
    }

    private void showProject(File dir) {
        shell(dir.getName());
        content.addView(text(tr("Project files", "ملفات المشروع"), 22, true));
        listFiles(dir, dir, 0);
        Button back = button(tr("Back to projects", "العودة للمشاريع"));
        back.setOnClickListener(v -> showHome());
        content.addView(back);
    }

    private void listFiles(File rootDir, File dir, int depth) {
        File[] files = dir.listFiles();
        if (files == null) return;
        Arrays.sort(files, (a,b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (File f : files) {
            TextView item = text((f.isDirectory() ? "📁 " : "📄 ") + f.getName(), 14, false);
            item.setPadding(dp(8 + depth*15), dp(8), dp(8), dp(8));
            item.setOnClickListener(v -> {
                if (f.isDirectory()) showProject(dirRoot(f, rootDir));
                else openTextFile(f);
            });
            content.addView(item);
            if (f.isDirectory()) listFiles(rootDir, f, depth+1);
        }
    }
    private File dirRoot(File ignored, File rootDir) { return rootDir; }

    private void openTextFile(File file) {
        if (file.length() > 1024*1024) { toast("File is larger than 1 MB; preview disabled."); return; }
        try {
            String value = new String(readAll(file), "UTF-8");
            EditText editor = new EditText(this);
            editor.setText(value);
            editor.setGravity(Gravity.TOP | Gravity.START);
            editor.setTextSize(13);
            editor.setTypeface(android.graphics.Typeface.MONOSPACE);
            editor.setTextColor(fg());
            editor.setBackgroundColor(panel());
            shell(file.getName());
            content.addView(editor, new LinearLayout.LayoutParams(-1, 0, 1));
            Button save = button(tr("Save file", "حفظ الملف"));
            save.setOnClickListener(v -> {
                try { write(file, editor.getText().toString()); toast(tr("Saved", "تم الحفظ")); }
                catch(Exception e) { toast(e.getMessage()); }
            });
            content.addView(save);
        } catch(Exception e) { toast(e.getMessage()); }
    }

    private void showSettings() {
        shell(tr("Settings", "الإعدادات"));
        addSection(tr("Appearance", "المظهر"));
        addChoice(tr("Theme", "السمة"), new String[]{"Black #000000", "Comfort Dark", "White"}, new String[]{"black","comfort","white"}, theme, value -> {
            theme = value; getPreferences(0).edit().putString("theme", theme).apply(); showSettings();
        });
        addSection(tr("Language", "اللغة"));
        String[] langs = {"العربية","English","Français","Deutsch","Español","Português","Русский","Türkçe","简体中文","日本語","한국어","हिन्दी","اردو","فارسی","Bahasa Indonesia","বাংলা"};
        addChoice(tr("App language", "لغة التطبيق"), langs, langs, language, value -> {
            language = value; getPreferences(0).edit().putString("language", language).apply(); showHome();
        });
        addSection("Storage & privacy");
        content.addView(cardText("Projects are stored in this app's private storage. ZIP import uses Android's document picker. API credentials and external account connections are not configured in this starter build."));
        addSection("About");
        content.addView(cardText("XCode Studio • 0.1.0\nJava • Android SDK • Local project workspace"));
    }

    interface Choice { void selected(String value); }
    private void addChoice(String title, String[] labels, String[] values, String selected, Choice callback) {
        Button b = button(title + ": " + selected);
        b.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle(title).setItems(labels, (d,which) -> callback.selected(values[which])).show());
        content.addView(b);
    }

    private void showGitHub() {
        shell("GitHub");
        content.addView(text("GitHub integration", 23, true));
        content.addView(cardText("Account sign-in is not connected yet. Configure a GitHub OAuth App or GitHub App, callback URI, and least-privilege permissions before enabling authentication."));
        Button open = button("Open GitHub");
        open.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/login"))));
        content.addView(open);
        content.addView(cardText("Planned: repository browser, multiple accounts, clone/import, commit, push, pull, Actions runs, logs, and artifact downloads."));
    }

    private void showBuild() {
        shell("Build & Security");
        content.addView(text("Build automation", 23, true));
        content.addView(cardText("The repository includes a sample GitHub Actions workflow for Android builds. Public repositories may receive eligible free Actions minutes under GitHub's current policy; private repositories consume the account's included quota and may incur charges. Limits and eligibility are controlled by GitHub."));
        content.addView(cardText("Security verification is a report of checks, not a guarantee that an app is completely safe. No Google Play upload or publication is performed."));
        Button docs = button("GitHub Actions billing documentation");
        docs.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.github.com/en/billing/concepts/product-billing/github-actions"))));
        content.addView(docs);
    }

    private void showAI() {
        shell("AI Providers");
        content.addView(text("Optional AI providers", 23, true));
        content.addView(cardText("Planned providers: OpenAI, Gemini, Anthropic Claude, OpenRouter, Groq, Mistral, DeepSeek, and compatible custom endpoints."));
        content.addView(cardText("This starter build does not collect or store API keys. Before enabling providers, implement encrypted credential storage, provider-specific authentication, user consent for code sharing, and clear usage/cost controls."));
    }

    private void addSection(String s) {
        TextView t = text(s, 18, true);
        t.setPadding(0, dp(20), 0, dp(8));
        content.addView(t);
    }
    private void addNavCard(String title, String subtitle, Runnable action) {
        LinearLayout c = card();
        c.addView(text(title, 16, true));
        c.addView(text(subtitle, 13, false));
        c.setOnClickListener(v -> action.run());
        content.addView(c);
    }
    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        c.setBackgroundColor(panel());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(10);
        c.setLayoutParams(lp);
        return c;
    }
    private TextView cardText(String s) {
        LinearLayout c = card(); TextView t = text(s, 14, false); c.addView(t); content.addView(c); return t;
    }
    private TextView text(String s, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(fg());
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setPadding(0, dp(4), 0, dp(4));
        return t;
    }
    private Button button(String s) {
        Button b = new Button(this); b.setText(s); b.setTextSize(12); return b;
    }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
    private String safeName(String s) { return s.replaceAll("[^A-Za-z0-9._ -]", "_").trim(); }
    private void write(File f, String s) throws Exception {
        try (FileOutputStream out = new FileOutputStream(f)) { out.write(s.getBytes("UTF-8")); }
    }
    private byte[] readAll(File f) throws Exception {
        try (InputStream in = new FileInputStream(f); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] b = new byte[8192]; int n; while ((n=in.read(b))!=-1) out.write(b,0,n); return out.toByteArray();
        }
    }
    private void deleteTree(File f) {
        if (f.isDirectory()) { File[] children = f.listFiles(); if (children != null) for (File c : children) deleteTree(c); }
        f.delete();
    }
}