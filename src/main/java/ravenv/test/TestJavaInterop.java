package ravenv.test;

/**
 * Tests Kotlin/Java interop by calling Kotlin functions from Java
 */
public class TestJavaInterop {

    public static void main(String[] args) {
        System.out.println("=== Kotlin/Java Interop Tests ===\n");

        // Test 1: Calling Kotlin string extension
        String result = TestKotlinUtils.INSTANCE.kotlinToUpperCase("hello from java");
        System.out.println("Test 1 - Uppercase: " + result);

        // Test 2: Kotlin joinToString
        String repeated = TestKotlinUtils.INSTANCE.repeatedString("RavenV", 3);
        System.out.println("Test 2 - Repeated: " + repeated);

        // Test 3: Nullable type handling
        int length1 = TestKotlinUtils.INSTANCE.processNullable("test");
        int length2 = TestKotlinUtils.INSTANCE.processNullable(null);
        System.out.println("Test 3 - Nullable (with value): " + length1);
        System.out.println("Test 3 - Nullable (null): " + length2);

        // Test 4: Data class
        TestKotlinUtils.ModInfo modInfo = TestKotlinUtils.INSTANCE.createModInfo("RavenV", "0.1.1");
        System.out.println("Test 4 - Data class: " + modInfo.getName() + " v" + modInfo.getVersion());

        // Test 5: Coroutine (blocking call from Java)
        System.out.println("Test 5 - Coroutine:");
        TestKotlinUtils.INSTANCE.testCoroutineBlocking();

        System.out.println("\n=== All tests completed ===");
    }

    /**
     * Helper method to verify interop from another Java class
     */
    public static String callKotlinFromJava(String input) {
        return TestKotlinUtils.INSTANCE.kotlinToUpperCase(input);
    }
}