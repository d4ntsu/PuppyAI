package com.puppy.service;

import java.util.List;
import java.util.Locale;

/**
 * Stand-in for the AI until milestone 4: returns short canned replies so the
 * chat interface can be built and tested without a provider.
 *
 * <p>This class will be replaced by {@code ChatService} and the AI providers; it
 * exists so the controller does not contain response logic.</p>
 */
public final class MockChatResponder {

    public static final String GREETING =
            "Hi! I'm Puppy, your desktop study buddy. What should we look at today?";

    private static final List<String> FALLBACKS = List.of(
            "Woof! I'm still a mock puppy for now, but I'll be able to help with that soon.",
            "That sounds interesting! Once my AI brain is wired up I'll explain it properly.",
            "I'm listening! For now I can only wag my tail at that.");
    private static final List<String> HELP_CUES = List.of("help", "how", "what", "why", "explain");
    private static final List<String> STUDY_CUES = List.of("study", "focus", "timer", "pomodoro");

    private int fallbackIndex;

    /** Returns a deterministic mock reply. Blank input is rejected by the caller. */
    public String replyTo(String message) {
        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, STUDY_CUES)) {
            return "Study mode is that way! Open the menu and pick STUDY — I'll read along with you.";
        }
        if (containsAny(normalized, HELP_CUES)) {
            return "I can chat, help you focus and, once I grow up, research things online. "
                    + "Double-click me to see the menu.";
        }
        String reply = FALLBACKS.get(fallbackIndex % FALLBACKS.size());
        fallbackIndex++;
        return reply;
    }

    private static boolean containsAny(String text, List<String> cues) {
        return cues.stream().anyMatch(text::contains);
    }
}
