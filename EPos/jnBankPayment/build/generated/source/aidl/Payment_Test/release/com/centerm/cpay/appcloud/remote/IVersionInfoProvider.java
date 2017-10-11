/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: F:\\xiangmubeifen\\0929zao\\EPos\\jnBankPayment\\src\\main\\aidl\\com\\centerm\\cpay\\appcloud\\remote\\IVersionInfoProvider.aidl
 */
package com.centerm.cpay.appcloud.remote;
/**
 * author: linwanliang</br>
 * date:2016/7/2</br>
 */
public interface IVersionInfoProvider extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.centerm.cpay.appcloud.remote.IVersionInfoProvider
{
private static final java.lang.String DESCRIPTOR = "com.centerm.cpay.appcloud.remote.IVersionInfoProvider";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.centerm.cpay.appcloud.remote.IVersionInfoProvider interface,
 * generating a proxy if needed.
 */
public static com.centerm.cpay.appcloud.remote.IVersionInfoProvider asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.centerm.cpay.appcloud.remote.IVersionInfoProvider))) {
return ((com.centerm.cpay.appcloud.remote.IVersionInfoProvider)iin);
}
return new com.centerm.cpay.appcloud.remote.IVersionInfoProvider.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getLatestVersion:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
com.centerm.cpay.appcloud.remote.IVersionInfoCallback _arg1;
_arg1 = com.centerm.cpay.appcloud.remote.IVersionInfoCallback.Stub.asInterface(data.readStrongBinder());
this.getLatestVersion(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.centerm.cpay.appcloud.remote.IVersionInfoProvider
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * 获取最新的版本信息
     *
     * @param pkgName  包名
     * @param callback 回调对象
     */
@Override public void getLatestVersion(java.lang.String pkgName, com.centerm.cpay.appcloud.remote.IVersionInfoCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pkgName);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_getLatestVersion, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getLatestVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
/**
     * 获取最新的版本信息
     *
     * @param pkgName  包名
     * @param callback 回调对象
     */
public void getLatestVersion(java.lang.String pkgName, com.centerm.cpay.appcloud.remote.IVersionInfoCallback callback) throws android.os.RemoteException;
}
