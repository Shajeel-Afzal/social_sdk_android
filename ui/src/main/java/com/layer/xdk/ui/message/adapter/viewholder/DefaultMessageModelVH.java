package com.layer.xdk.ui.message.adapter.viewholder;

import android.databinding.Observable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.view.View;
import android.view.ViewGroup;

import com.layer.xdk.ui.BR;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.databinding.XdkUiMessageModelVhDefaultBinding;
import com.layer.xdk.ui.message.container.MessageContainer;

public class DefaultMessageModelVH extends
        MessageModelVH<DefaultMessageModelVHModel, XdkUiMessageModelVhDefaultBinding > {

    // Cache this so we know not to re-set the bias on the constraint layout
    private Boolean mCurrentlyMyMessage;

    public DefaultMessageModelVH(ViewGroup parent, final DefaultMessageModelVHModel viewModel) {
        super(parent, R.layout.xdk_ui_message_model_vh_default, viewModel);

        getBinding().setViewHolderModel(viewModel);

        getBinding().getRoot().setClickable(true);
        getBinding().getRoot().setOnLongClickListener(viewModel.getOnLongClickListener());

        getBinding().executePendingBindings();
    }

    @Override
    void onBind() {
        MessageContainer messageContainer =
                (MessageContainer) getBinding().messageViewStub.getRoot();
        if (messageContainer != null) {
            messageContainer.setMessageModel(getItem());
        }

        getViewHolderModel().update();
    }

    public MessageContainer inflateViewContainer(int containerLayoutId) {
        getBinding().messageViewStub.getViewStub().setLayoutResource(containerLayoutId);
        MessageContainer container =
                (MessageContainer) getBinding().messageViewStub.getViewStub().inflate();
        getViewHolderModel().addOnPropertyChangedCallback(new AlphaAndBiasObserver(container));
        return container;
    }

    public View.OnLongClickListener getLongClickListener() {
        return getViewHolderModel().getOnLongClickListener();
    }

    private class AlphaAndBiasObserver extends Observable.OnPropertyChangedCallback {
        private final View mInflated;

        public AlphaAndBiasObserver(View inflated) {
            mInflated = inflated;
        }

        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            if (propertyId == BR.messageCellAlpha || propertyId == BR._all) {
                mInflated.setAlpha(getViewHolderModel().getMessageCellAlpha());
            }
            if ((propertyId == BR._all || propertyId == BR.myMessage)
                    && (mCurrentlyMyMessage == null || getViewHolderModel().isMyMessage() != mCurrentlyMyMessage)) {
                mCurrentlyMyMessage = getViewHolderModel().isMyMessage();
                ConstraintSet set = new ConstraintSet();
                ConstraintLayout parent =
                        (ConstraintLayout) mInflated.getParent();
                set.clone(parent);
                set.setHorizontalBias(mInflated.getId(),
                        getViewHolderModel().isMyMessage() ? 1.0f : 0.0f);
                set.applyTo(parent);
            }
        }
    }
}
