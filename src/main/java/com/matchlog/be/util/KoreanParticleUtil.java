package com.matchlog.be.util;

public final class KoreanParticleUtil {

    private KoreanParticleUtil() {}

    // 마지막 글자 받침 여부로 조사 선택 (한글 외 문자는 consonant 기본값 사용)
    public static String select(String word, String consonant, String vowel) {
        if (word == null || word.isEmpty()) return consonant;
        char last = word.charAt(word.length() - 1);
        int code = last - 0xAC00;
        if (code < 0 || code > 11171) return consonant;
        return code % 28 == 0 ? vowel : consonant;
    }
}
