package com.team420.kekhunter.gps;

public interface KaliGPSUpdates {

    interface Receiver {
        void onPositionUpdate(String nmeaSentences);

        void onFirstPositionUpdate();
    }

    interface Provider {
        void onLocationUpdatesRequested(Receiver receiver);

        boolean onReceiverReattach(Receiver receiver);

        void onStopRequested();
    }


}
