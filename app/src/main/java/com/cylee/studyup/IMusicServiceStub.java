package com.cylee.studyup;

import android.os.RemoteException;

public interface IMusicServiceStub {
    long getCurrentPosition() throws RemoteException;

    boolean isPlaying() throws RemoteException;

    long getListCount() throws RemoteException;

    void playPause() throws RemoteException;

    void next() throws RemoteException;

    void prev() throws RemoteException;

    void seekTo(int pos) throws RemoteException;
}
