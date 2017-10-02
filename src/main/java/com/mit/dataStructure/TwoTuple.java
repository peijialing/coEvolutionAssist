package com.mit.dataStructure;

/**
 * Created by peijialing on 27/9/2017.
 */

public class TwoTuple<A, B> {

        public final A first;

        public final B second;

        public TwoTuple(A a, B b){
            first = a;
            second = b;
        }

        public A getFirst() {
            return first;
        }
        public B getSecond() {
            return second;
        }
        public String toString(){
            return "(" + first + ", " + second + ")";
        }

}


