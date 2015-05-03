package com.matchrace.matchrace.modules;

import java.util.Stack;

/**
 * Created by nehorg on 5/3/15.
 */
public class MyStack {
    private Stack<String> st;

    public MyStack()
    {
        st = new Stack<String>();
    }

    public synchronized String pop()
    {
        if (!isEmpty())
            return st.pop();
        return null;
    }

    public synchronized String push(String s)
    {
        st.push(s);
        return null;
    }

    public synchronized boolean isEmpty()
    {
        return st.isEmpty();
    }
}
