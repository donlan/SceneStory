package gui.dong.scenestory.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import gui.dong.scenestory.R;

public class LearnCommitFragment extends BottomSheetDialogFragment{

    public static void show(FragmentManager fragmentManager,OnCommitClickListener commitClickListener){
        LearnCommitFragment fragment = new LearnCommitFragment();
        fragment.setCommitClickListener(commitClickListener);
        fragment.show(fragmentManager,"");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learn_commit,container,false);
        view.findViewById(R.id.commit_no_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(commitClickListener!=null){
                    commitClickListener.onCommitClick(false);
                }
            }
        });
        view.findViewById(R.id.commit_yes_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(commitClickListener!=null){
                    commitClickListener.onCommitClick(true);
                }
            }
        });
        return view;
    }

    public void setCommitClickListener(OnCommitClickListener commitClickListener) {
        this.commitClickListener = commitClickListener;
    }

    private OnCommitClickListener commitClickListener;
    public interface OnCommitClickListener{
        void onCommitClick(boolean yes);
    }
}
