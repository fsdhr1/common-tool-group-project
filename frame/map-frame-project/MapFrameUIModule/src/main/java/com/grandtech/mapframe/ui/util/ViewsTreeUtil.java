package com.grandtech.mapframe.ui.util;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ViewsTreeUtil
 * @Description TODO View帮助类
 * @Author: fs
 * @Date: 2021/5/13 10:15
 * @Version 2.0
 */
 public final class ViewsTreeUtil {
    private ViewsTreeUtil() {
    }

    public static List<View> find(ViewGroup root, Object tag) {
        FinderByTag finderByTag = new FinderByTag(tag);
        LayoutTraverser.build(finderByTag).traverse(root);
        return finderByTag.getViews();
    }

    public static <T extends View> List<T> findT(ViewGroup root, Object tag) {
        FinderByTag finderByTag = new FinderByTag(tag);
        LayoutTraverser.build(finderByTag).traverse(root);
        List<View> views = finderByTag.getViews();
        List<T> res = new ArrayList<>();
        View view;
        for (int i = 0; i < views.size(); i++) {
            view = views.get(i);
            try {
                res.add((T) view);
            } catch (Exception e) {
                e.getMessage();
            }
        }
        return res;
    }


    public static <T extends View> List<T> find(ViewGroup root, Class<T> type) {
        FinderByType<T> finderByType = new FinderByType<T>(type);
        LayoutTraverser.build(finderByType).traverse(root);
        return finderByType.getViews();
    }

    private static class FinderByTag implements LayoutTraverser.Processor {
        private final Object searchTag;
        private final List<View> views = new ArrayList<>();

        private FinderByTag(Object searchTag) {
            this.searchTag = searchTag;
        }

        @Override
        public void process(View view) {
            Object viewTag = view.getTag();

            if (viewTag != null && viewTag.equals(searchTag)) {
                views.add(view);
            }
        }

        private List<View> getViews() {
            return views;
        }
    }

    private static class FinderByType<T extends View> implements LayoutTraverser.Processor {
        private final Class<T> type;
        private final List<T> views;

        private FinderByType(Class<T> type) {
            this.type = type;
            views = new ArrayList<T>();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void process(View view) {
            if (type.isInstance(view)) {
                views.add((T) view);
            }
        }

        public List<T> getViews() {
            return views;
        }
    }


    public static List<View> getAllChildViews(View view) {
        List<View> allChildren = new ArrayList<>();
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewChild = vp.getChildAt(i);
                allChildren.add(viewChild);
                allChildren.addAll(getAllChildViews(viewChild));
            }
        }
        return allChildren;
    }


    public static boolean hasView(View parentView, View view) {
        View _view;
        boolean flag = false;
        if (parentView instanceof ViewGroup) {
            try {
                int id = view.getId();
                _view = parentView.findViewById(id);
                flag = _view != null;
            } catch (Exception e) {
                List<View> views = getAllChildViews(parentView);
                flag = views != null && views.contains(view);
            }
        }
        return flag;
    }

}
