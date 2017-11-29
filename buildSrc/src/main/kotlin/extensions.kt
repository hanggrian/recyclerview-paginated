import org.gradle.api.artifacts.dsl.DependencyHandler

const val bintrayUser = "hendraanggrian"
const val bintrayGroup = "com.hendraanggrian"
const val bintrayArtifact = "recyclerview-paginated"
const val bintrayPublish = "0.6"
const val bintrayDesc = "Android endless scrolling in its simplest form"
const val bintrayWeb = "https://github.com/hendraanggrian/recyclerview-paginated"

const val minSdk = 14
const val targetSdk = 27
const val buildTools = "27.0.1"

const val kotlinVersion = "1.1.61"
const val supportVersion = "27.0.1"

const val kotaVersion = "0.19"
const val circleimageviewVersion = "2.1.0"
const val rxjavaVersion = "2.1.6"
const val rxkotlinVersion = "2.1.0"
const val rxandroidVersion = "2.0.1"
const val retrofitVersion = "2.3.0"
const val okhttpVersion = "3.8.1"

const val junitVersion = "4.12"
const val truthVersion = "0.36"
const val runnerVersion = "1.0.1"
const val espressoVersion = "3.0.1"

fun DependencyHandler.support(module: Any, version: Any, vararg groupSuffixes: Any) = "${StringBuilder("com.android.support").apply { groupSuffixes.forEach { append(".$it") } }}:$module:$version"
fun DependencyHandler.square(module1: Any, module2: Any, version: Any) = "com.squareup.$module1:$module2:$version"
fun DependencyHandler.rx(module: String, version: String) = "io.reactivex.rxjava2:rx$module:$version"
fun DependencyHandler.hendraanggrian(module: Any, version: Any) = "com.hendraanggrian:$module:$version"
fun DependencyHandler.junit(version: Any) = "junit:junit:$version"
fun DependencyHandler.google(module: Any, version: Any) = "com.google.$module:$module:$version"