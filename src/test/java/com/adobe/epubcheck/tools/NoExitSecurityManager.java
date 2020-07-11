package com.adobe.epubcheck.tools;

import java.security.Permission;

/**
 * Created with IntelliJ IDEA.
 * User: apond
 * Date: 6/4/12
 * Time: 9:24 AM
 * To change this template use File | Settings | File Templates.
 */


public class NoExitSecurityManager extends SecurityManager
{

  public static class ExitException extends SecurityException
  {
    public final int status;

    public ExitException(int status)
    {
      super("There is no escape!");
      this.status = status;
    }
  }

  @Override
  public void checkPermission(Permission perm)
  {                 // allow anything.
  }

  @Override
  public void checkPermission(Permission perm, Object context)
  {                 // allow anything.
  }

  @Override
  public void checkExit(int status)
  {
    super.checkExit(status);
    throw new ExitException(status);
  }
}
