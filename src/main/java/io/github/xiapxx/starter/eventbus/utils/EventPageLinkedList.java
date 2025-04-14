package io.github.xiapxx.starter.eventbus.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author xiapeng
 * @Date 2025-04-11 16:07
 */
public class EventPageLinkedList<EVENT> implements Iterable<List<EVENT>> {

    private Iterator<EVENT> dataIterator;

    private int pageSize;

    public EventPageLinkedList(Collection<EVENT> dataColl, int pageSize) {
        if (dataColl == null || dataColl.isEmpty()) {
            return;
        }
        this.pageSize = pageSize <= 0 ? 10 : pageSize;
        this.dataIterator = dataColl.iterator();
    }

    @Override
    public Iterator<List<EVENT>> iterator() {
        return new PageListIterator();
    }

    private class PageListIterator implements Iterator<List<EVENT>> {

        @Override
        public boolean hasNext() {
            if(dataIterator == null){
                return false;
            }
            return dataIterator.hasNext();
        }

        @Override
        public List<EVENT> next() {
            if(dataIterator == null){
                return null;
            }
            int nextSize = 0;
            List<EVENT> nextList = new LinkedList<>();
            while (dataIterator.hasNext()) {
                nextList.add(dataIterator.next());
                nextSize = nextSize + 1;
                if(nextSize >= pageSize){
                    break;
                }
            }
            return nextList;
        }
    }
}
