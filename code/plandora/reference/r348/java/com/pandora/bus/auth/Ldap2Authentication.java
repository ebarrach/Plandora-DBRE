package com.pandora.bus.auth;

import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.pandora.PreferenceTO;
import com.pandora.UserTO;
import com.pandora.delegate.UserDelegate;
import com.pandora.exception.BusinessException;

public class Ldap2Authentication extends Authentication {

	public boolean authenticate(UserTO fullUTo, String formPassword,
			boolean isPassEncrypted) throws BusinessException {
		boolean response = true;
		UserDelegate udel = new UserDelegate();
		UserTO root = udel.getRoot();

		 
		String host = root.getPreference()
				.getPreference(PreferenceTO.LDAP_HOST);
		String uid = root.getPreference().getPreference(
				PreferenceTO.LDAP_UID_REGISTER);
		
		String reg = uid.replaceAll("<USER>", fullUTo.getUsername());
		
		
		String[] userData = reg.split( Pattern.quote("|")); 
		

		LdapContext ctx = null;
		try {
			ctx = this.getConnection(userData[0].trim(), formPassword, host);
		} catch (NamingException e) {
			response = false;
			throw new BusinessException(e.getMessage());
		} finally {
			try {
				if (ctx != null) {
					ctx.close();
				}
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}

		return response;
	}

	public String getUniqueName() {
		return "label.authMode.LDAP2";
	}

	@Override
	public String encrypt(String rawPassword) throws BusinessException {
		return null;
	}

	// **************************************************************************
	// ** getConnection
	// *************************************************************************/
	/**
	 * Used to authenticate a user given a username/password and domain name.
	 * Provides an option to identify a specific a Active Directory server.
	 */
	private LdapContext getConnection(String username, String password,
			String serverName) throws NamingException {

		if (password != null) {
			password = password.trim();
			if (password.length() == 0)
				password = null;
		}

		// bind by using the specified username/password
		Hashtable<String, String> props = new Hashtable<String, String>();

		props.put(Context.SECURITY_PRINCIPAL, username);
		if (password != null) {
			props.put(Context.SECURITY_CREDENTIALS, password);
		}

		String ldapURL = "ldap://" + serverName + '/';
		props.put(Context.SECURITY_AUTHENTICATION, "simple");
		props.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		props.put(Context.PROVIDER_URL, ldapURL);
		try {
			return new InitialLdapContext(props, null);
		} catch (javax.naming.CommunicationException e) {
			throw new NamingException("Failed to connect to " + username
					+ ((serverName == null) ? "" : " through " + serverName));
		} catch (NamingException e) {
			throw e;
		}
	}

	public void changePassword(UserTO uto) throws Exception {
		LdapContext ctx = null;
		UserDelegate udel = new UserDelegate();
		try {

			UserTO root = udel.getRoot();
			String user = root.getPreference().getPreference(
					PreferenceTO.LDAP_UID_REGISTER);
			
			String host = root.getPreference()
					.getPreference(PreferenceTO.LDAP_HOST);
			
			String[] userData = user.split( Pattern.quote("|")); 
			

			ctx = getConnection(userData[1].trim(),
					userData[2].trim(),host);
			ModificationItem[] mods = new ModificationItem[2];
			String oldQuotedPassword = "\"" + uto.getGenericTag() + "\"";
			byte[] oldUnicodePassword = oldQuotedPassword.getBytes("UTF-16LE");
			String newQuotedPassword = "\"" + uto.getPassword() + "\"";
			byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

			mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
					new BasicAttribute("unicodePwd", oldUnicodePassword));
			mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("unicodePwd", newUnicodePassword));

			String theUserName = "CN=" + uto.getUsername()
					+ ",OU=Usuarios,OU=Unisoma,DC=unisoma,DC=com";
			// Perform the update
			ctx.modifyAttributes(theUserName, mods);
			System.out.println("Changed Password for successfully");

		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}

	}

}