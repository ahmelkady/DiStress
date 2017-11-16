package com.beacodes;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Arrays;

public final class BeacodeScanner {
    public enum State {
        STOPPED (0),
        MIC_IN_USE (1),
        RUNNING (2);

        private final int state;
        State(int state) { this.state = state; }

        public static State getState(int state) {
            switch (state) {
                case 0: return STOPPED;
                case 1: return MIC_IN_USE;
                case 2: return RUNNING;
                default: throw new IllegalArgumentException();
            }
        }
    }

    public enum Tag {
        RAW (0),
        TEXT (1);

        private final int tag;
        Tag(int tag) {
            this.tag = tag;
        }

        public static Tag getTag(int tag) {
            switch (tag) {
                case 0: return RAW;
                case 1: return TEXT;
                default: throw new IllegalArgumentException();
            }
        }
    }

    public static class Message {
        private int id;
        private boolean uidIncluded;
        private int uid;
        private Tag tag;
        private Object payload;

        public Message(int id, boolean uidIncluded, int uid, Tag tag, Object payload) {
            this.id = id;
            this.uidIncluded = uidIncluded;
            this.uid = uid;
            this.tag = tag;
            this.payload = payload;
        }

        public int getId() { return id; }

        public boolean isUidIncluded() {
            return uidIncluded;
        }

        public int getUid() {
            if (!uidIncluded)
                throw new IllegalStateException("Uid is not included");
            return uid;
        }

        public Tag getTag() {
            return tag;
        }

        public Object getPayload() {
            return payload;
        }

        // convenience methods

        public byte[] getRawPayload() {
            return (byte[])payload;
        }

        public String getStringPayload() {
            return (String)payload;
        }

        @Override
        public String toString() {
            String r = "";
            switch (getTag()) {
                case RAW:
                    byte[] raw = getRawPayload();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < raw.length; ++i) {
                        String hex = Integer.toHexString(raw[i] + 128);
                        if (hex.length() == 1)
                            sb.append('0');
                        sb.append(hex);
                        if (i < raw.length - 1)
                            sb.append('-');
                    }
                    r = sb.toString();
                    break;
                case TEXT:
                    r = getStringPayload();
                    break;
            }
            return r;
        }
    }

    public interface Listener {
        void onScannerStateChange(State oldState, State newState);
        void onPartialMessage(int messageId, int percent);
        void onPartialMessageCancelled(int messageId);
        void onMessage(int messageId, Message message);
    }

    private static Listener listener;

    private BeacodeScanner() { }

    static {
        System.loadLibrary("beacode");
    }

    public static void setListener(Listener listener) {
        BeacodeScanner.listener = listener;
    }
    public static Listener getListener() { return BeacodeScanner.listener; }
    public static State getState() { return State.getState(getStateID()); }
    private static native int getStateID();
    public static native void start();
    public static native void stop();
    public static native boolean setProfile(int profile); // 0 = baseline, 1 = better
    public static native boolean setChannelSpecs(long[] channelSpecs); // must not be called when in RUNNING state
    public static native long getStandardChannelSpec(int channel); // 0 .. 7

    // STOPPED(0), MIC_IN_USE(1), RUNNING(2)
    private static void state_change(final int old_state, final int new_state) {
        Log.d("BeacodeScanner", "state_change, old: " + old_state + ", new: " + new_state);
        if (listener != null) {
            Handler th = new Handler(Looper.getMainLooper());
            th.post(new Runnable() {
                @Override
                public void run() {
                    listener.onScannerStateChange(State.getState(old_state), State.getState(new_state));
                }
            });
        }
    }

    private static void payload_partial(final int payload_id, final int percent) {
        Log.d("BeacodeScanner", "payload_partial, id: " + payload_id + ", percent: " + percent);
        if (listener != null) {
            Handler th = new Handler(Looper.getMainLooper());
            th.post(new Runnable() {
                @Override
                public void run() {
                    listener.onPartialMessage(payload_id, percent);
                }
            });
        }
    }

    private static void payload_cancel(final int payload_id) {
        Log.d("BeacodeScanner", "payload_cancel, id: " + payload_id);
        if (listener != null) {
            Handler th = new Handler(Looper.getMainLooper());
            th.post(new Runnable() {
                @Override
                public void run() {
                    listener.onPartialMessageCancelled(payload_id);
                }
            });
        }
    }

    // FULL payload callbacks:

    private static void raw_payload(final int payload_id, final boolean has_uid, final int uid, final byte[] raw) {
        Log.d("BeacodeScanner", "raw_payload, has_uid: " + has_uid + ", uid: " + uid + ", raw: " + Arrays.toString(raw));
        if (listener != null) {
            Handler th = new Handler(Looper.getMainLooper());
            th.post(new Runnable() {
                @Override
                public void run() {
                    listener.onMessage(payload_id, new Message(payload_id, has_uid, uid, Tag.RAW, raw));
                }
            });
        }
    }

    private static void string_payload(final int payload_id, final boolean has_uid, final int uid, byte tag, final String str) {
        Log.d("BeacodeScanner", "string_payload, has_uid: " + has_uid + ", uid: " + uid + ", tag: " + tag + ", str: " + str);
        if (listener != null) {
            final Tag tagEnum = Tag.getTag(tag);
            Handler th = new Handler(Looper.getMainLooper());
            th.post(new Runnable() {
                @Override
                public void run() {
                    listener.onMessage(payload_id, new Message(payload_id, has_uid, uid, tagEnum, str));
                }
            });
        }
    }
}
