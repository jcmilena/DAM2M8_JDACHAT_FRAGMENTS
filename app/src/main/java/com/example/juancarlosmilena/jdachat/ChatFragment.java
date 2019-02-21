package com.example.juancarlosmilena.jdachat;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;



public class ChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView chatRecycler;
    List<String> messageList = new ArrayList<>();
    ChatAdapter miAdapter = new ChatAdapter(messageList);

    OnChatListener mListener;


    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.

     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        chatRecycler = view.findViewById(R.id.chatRecycler);
        chatRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecycler.setAdapter(miAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL);
        chatRecycler.addItemDecoration(dividerItemDecoration);

        mListener.getFirebaseChat(mParam1);

        return view;

    }






    public class ChatViewHolder extends RecyclerView.ViewHolder{

        TextView emailTextView;


        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            emailTextView = itemView.findViewById(R.id.emailTextView);
        }
    }

    public class ChatAdapter extends RecyclerView.Adapter<ChatFragment.ChatViewHolder>{

        List<String> chatlistadapter = new ArrayList<>();

        public ChatAdapter(List<String> userslistadapter) {
            this.chatlistadapter = userslistadapter;
        }

        @NonNull
        @Override
        public ChatFragment.ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemview = getLayoutInflater()
                    .inflate(R.layout.user_viewholder, viewGroup, false);
            return new ChatFragment.ChatViewHolder(itemview);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder chatViewHolder, int i) {

            chatViewHolder.emailTextView
                    .setText(messageList.get(i));

        }


        @Override
        public int getItemCount() {
            return messageList.size();
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        //Eliminamos el Listener para no crear múltiples Listeners
        //apuntando al mismo chat
        mListener.removeChatListener();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChatListener) {
            mListener = (OnChatListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnChatListener{

        void getFirebaseChat(String chat);
        void removeChatListener();
    }

    public void addMessageToList(String msg){

        messageList.add(msg);
        miAdapter.notifyDataSetChanged();
    }
}
