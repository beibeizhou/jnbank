/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: F:\\xiangmubeifen\\0929zao\\EPos\\jnBankPayment\\src\\main\\aidl\\com\\centerm\\cpay\\appcloud\\remote\\IVersionInfoCallback.aidl
 */
package com.centerm.cpay.appcloud.remote;
/**
 * author: linwanliang</br>
 * date:2016/7/2</br>
 */
public interface IVersionInfoCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.centerm.cpay.appcloud.remote.IVersionInfoCallback
{
private static final java.lang.String DESCRIPTOR = "com.centerm.cpay.appcloud.remote.IVersionInfoCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.centerm.cpay.appcloud.remote.IVersionInfoCallback interface,
 * generating a proxy if needed.
 */
public static com.centerm.cpay.appcloud.remote.IVersionInfoCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.centerm.cpay.appcloud.remote.IVersionInfoCallback))) {
return ((com.centerm.cpay.appcloud.remote.IVersionInfoCallback)iin);
}
return new com.centerm.cpay.appcloud.remote.IVersionInfoCallback.Stub.Proxy(obj);
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
case TRANSACTION_onSuccess:
{
data.enforceInterface(DESCRIPTOR);
com.centerm.cpay.appcloud.remote.VersionInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = com.centerm.cpay.appcloud.remote.VersionInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onSuccess(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onError:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
this.onError(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.centerm.cpay.appcloud.remote.IVersionInfoCallback
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
     * 查询成功
     *
     * @param info 版本信息
     */
@Override public void onSuccess(com.centerm.cpay.appcloud.remote.VersionInfo info) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((info!=null)) {
_data.writeInt(1);
info.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onSuccess, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 查询失败
     *
     * @param errorCode 错误码
     * @param errorInfo 错误信息
     */
@Override public void onError(int errorCode, java.lang.String errorInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(errorCode);
_data.writeString(errorInfo);
mRemote.transact(Stub.TRANSACTION_onError, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onSuccess = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * 查询成功
     *
     * @param info 版本信息
     */
public void onSuccess(com.centerm.cpay.appcloud.remote.VersionInfo info) throws android.os.RemoteException;
/**
     * 查询失败
     *
     * @param errorCode 错误码
     * @param errorInfo 错误信息
     */
public void onError(int errorCode, java.lang.String errorInfo) throws android.os.RemoteException;
}
