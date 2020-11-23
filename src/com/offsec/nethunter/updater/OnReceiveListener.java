package com.offsec.nethunter.updater;

public interface OnReceiveListener {

    /**
     * Listener called once HTTP GET request for json file with update data is processed.
     *
     * @param status response code from HTTP request
     * @param isBlocking whenever update is blocking
     * @param result response data from HTTP request
     * @return return true to show default library dialog, false otherwise
     */
    boolean onReceive(int status, boolean isBlocking, String result);
}
