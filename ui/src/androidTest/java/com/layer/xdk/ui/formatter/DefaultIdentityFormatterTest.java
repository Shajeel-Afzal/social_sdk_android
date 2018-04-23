package com.layer.xdk.ui.formatter;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.identity.DefaultIdentityFormatter;
import com.layer.xdk.ui.mock.MockIdentity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DefaultIdentityFormatterTest {

    private DefaultIdentityFormatter mIdentityFormatter;
    private Identity mMockIdentity;

    @Before
    public void setup() {
        mIdentityFormatter = new DefaultIdentityFormatter(InstrumentationRegistry.getTargetContext());
        mMockIdentity = new MockIdentity();
    }

    @Test
    public void testGetFirstName() {
        assertThat(mIdentityFormatter.getFirstName(mMockIdentity), is(mMockIdentity.getFirstName()));
    }

    @Test
    public void testGetLastName() {
        assertThat(mIdentityFormatter.getLastName(mMockIdentity), is(mMockIdentity.getLastName()));
    }

    @Test
    public void testGetDiplayName() {
        assertThat(mIdentityFormatter.getDisplayName(mMockIdentity), is(mMockIdentity.getDisplayName()));
    }

    @Test
    public void testGetInitials() {
        assertThat(mIdentityFormatter.getInitials(mMockIdentity), is("" + mMockIdentity.getFirstName().charAt(0) + mMockIdentity.getLastName().charAt(0)));
    }
}
