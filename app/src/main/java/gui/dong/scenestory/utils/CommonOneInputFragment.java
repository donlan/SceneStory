package gui.dong.scenestory.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import gui.dong.scenestory.R;


public class CommonOneInputFragment extends BottomSheetDialogFragment {


    public static CommonOneInputFragment newInstance(String title,String hintText,CommonInputListener inputListener){
        CommonOneInputFragment fragment = new CommonOneInputFragment();
        fragment.setTitle(title);
        fragment.setHintText(hintText);
        fragment.setInputListener(inputListener);
        return fragment;
    }

    private void setHintText(String hintText){
        this.hintText = hintText;
    }

    private void setInputListener(CommonInputListener inputListener) {
        this.inputListener = inputListener;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    private CommonInputListener inputListener;
    private EditText input;
    private String title;
    private String hintText;
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.common_ont_input_fragment,container,false);
        view.findViewById(R.id.common_one_input_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        TextView titleTv = (TextView) view.findViewById(R.id.common_one_input_title);
        titleTv.setText(title);
        view.findViewById(R.id.common_one_input_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputListener != null){
                    inputListener.onInputCommit(input.getText().toString());
                }
                dismiss();
            }
        });
        input = (EditText) view.findViewById(R.id.common_one_input_et);
        input.setHint(hintText);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        input = null;
        inputListener = null;
    }
}
