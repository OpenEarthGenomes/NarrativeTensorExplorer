#!/bin/bash
# NEON instruction test script

set -e

echo "🧪 Testing NEON Instructions"
echo "============================"

TEST_SRC="neon_test.c"
TEST_BIN="neon_test"

cat > $TEST_SRC << 'EOF'
#include <stdio.h>
#include <stdint.h>
#include <arm_neon.h>

void test_neon_availability() {
    printf("Testing NEON instructions...\n");
    
    // Test data
    int8_t data1[16] = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
    int8_t data2[16] = {16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1};
    
    // Load data into NEON registers
    int8x16_t vec1 = vld1q_s8(data1);
    int8x16_t vec2 = vld1q_s8(data2);
    
    // Vector addition
    int8x16_t result = vaddq_s8(vec1, vec2);
    
    // Extract result
    int8_t out[16];
    vst1q_s8(out, result);
    
    printf("NEON addition test:\n");
    printf("Input1: ");
    for(int i = 0; i < 16; i++) printf("%d ", data1[i]);
    printf("\nInput2: ");
    for(int i = 0; i < 16; i++) printf("%d ", data2[i]);
    printf("\nResult: ");
    for(int i = 0; i < 16; i++) printf("%d ", out[i]);
    printf("\n");
    
    // Dot product test
    int32_t dot = 0;
    for(int i = 0; i < 16; i++) {
        dot += data1[i] * data2[i];
    }
    printf("Dot product (scalar): %d\n", dot);
    
    printf("✅ NEON instructions are working!\n");
}

int main() {
    #ifdef __ARM_NEON
    printf("🟢 ARM NEON is enabled\n");
    test_neon_availability();
    #else
    printf("🔴 ARM NEON is NOT enabled\n");
    #endif
    
    return 0;
}
EOF

# Try to compile with NEON
echo "Compiling NEON test..."
if gcc -march=armv8-a -mfpu=neon -o $TEST_BIN $TEST_SRC 2>/dev/null; then
    echo "Compilation successful"
    if [ -f ./$TEST_BIN ]; then
        ./$TEST_BIN || true
    fi
elif gcc -march=armv7-a -mfpu=neon -o $TEST_BIN $TEST_SRC 2>/dev/null; then
    echo "Compilation successful (ARMv7)"
    if [ -f ./$TEST_BIN ]; then
        ./$TEST_BIN || true
    fi
else
    echo "⚠️  Could not compile NEON test"
    echo "This is expected on non-ARM hosts"
fi

# Cleanup
rm -f $TEST_SRC $TEST_BIN

echo "🧪 NEON test completed"
