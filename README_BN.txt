দৈনিক হিসাব — Android অ্যাপ সোর্স প্রজেক্ট

ফিচার:
- ব্যক্তিগত আয় ও ব্যয় যোগ
- ক্যাটাগরি নির্বাচন
- তারিখ ও বিবরণ
- মোট আয়, মোট ব্যয় ও অবশিষ্ট ব্যালেন্স
- লেনদেনের তালিকা
- কোনো লেনদেন মুছতে তালিকার আইটেমে লং-প্রেস
- ডেটা ফোনের SQLite ডাটাবেসে থাকে; ইন্টারনেট লাগে না

APK বানানোর ধাপ:
1. কম্পিউটারে Android Studio ইনস্টল করুন।
2. ZIP ফাইল Extract করুন।
3. Android Studio > Open > Extract করা DailyHisab ফোল্ডার নির্বাচন করুন।
4. Gradle Sync শেষ হওয়া পর্যন্ত অপেক্ষা করুন (প্রথমবার ইন্টারনেট লাগবে)।
5. Build > Build Bundle(s) / APK(s) > Build APK(s) চাপুন।
6. APK সাধারণত app/build/outputs/apk/debug/app-debug.apk-এ পাবেন।
7. APK ফোনে পাঠিয়ে ইনস্টল করুন। ফোনে "Install unknown apps" অনুমতি চাইতে পারে।

দ্রষ্টব্য:
- এটি সোর্স প্রজেক্ট; এখানে সরাসরি APK কম্পাইল করা হয়নি।
- PDF/Excel রিপোর্ট, ক্লাউড ব্যাকআপ, পাসওয়ার্ড লক ও একাধিক ডিভাইস সিঙ্ক এই প্রথম সংস্করণে নেই।
- Android Studio-তে SDK 35 এবং Gradle plugin ডাউনলোডের জন্য ইন্টারনেট দরকার।

ফোন থেকেই ফ্রি Cloud Build (GitHub Actions):
1. GitHub.com-এ ফ্রি অ্যাকাউন্ট খুলে New repository তৈরি করুন (Public রাখলে GitHub-hosted public runner-এর বিলিং সুবিধা প্রযোজ্য হতে পারে)।
2. ZIP Extract করে সব project file repository-তে upload করুন; .github/workflows/android.yml ফাইলটিও অবশ্যই আপলোড করবেন।
3. Repository-এর Actions tab খুলুন। "Build Daily Hisab APK" workflow নির্বাচন করে Run workflow চাপুন।
4. Build শেষ হলে নিচে Artifacts থেকে DailyHisab-debug-apk ZIP ডাউনলোড করুন।
5. ZIP Extract করে app-debug.apk ফোনে ইনস্টল করুন।
দ্রষ্টব্য: GitHub-এর runner/নীতি ও quota সময়ে সময়ে বদলাতে পারে। এই workflow-তে Gradle runner-এ আগে থেকে ইনস্টল থাকা ধরে নেওয়া হয়েছে।
