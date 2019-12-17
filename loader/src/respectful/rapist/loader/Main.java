package respectful.rapist.loader;

import net.minecraft.launchwrapper.Launch;
import respectful.rapist.loader.transformer.transformers.Entity;
import respectful.rapist.loader.transformer.transformers.GuiIngame;
import respectful.rapist.loader.transformer.transformers.Minecraft;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

public class Main {
    public static Loader loader;
    public static Class eventManager;
    public static Class hitBoxes;
    public static Method onKey, onRender, onTick;
    public static Instrumentation inst;
    public static byte[] origMinecraft, origEntity, origGuiIngame;

    static {
        try {
            URL[] URLs = {new URL("http://localhost:1337/client.jar")};
            loader = new Loader(URLs);
            eventManager = loader.findClass("respectful.rapist.client.EventManager");
            hitBoxes = loader.findClass("respectful.rapist.client.module.modules.HitBoxes");
            onKey = eventManager.getDeclaredMethod("onKey", int.class);
            onRender = eventManager.getDeclaredMethod("onRender");
            onTick = eventManager.getDeclaredMethod("onTick");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void agentmain(String args, Instrumentation inst) {
        Main.inst = inst;
        Launch.classLoader.addURL(Main.class.getProtectionDomain().getCodeSource().getLocation());
        transform(true);
    }

    private static void transform(boolean transforming) {
        ClassFileTransformer transformer;
        for (Class clazz : getInst().getAllLoadedClasses()) {
            switch (clazz.getName()) {
                case "net.minecraft.client.Minecraft":
                    if (transforming) {
                        transformer = new Minecraft();
                    } else {
                        transformer = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> getOrigMinecraft();
                    }
                    break;
                case "net.minecraft.entity.Entity":
                    if (transforming) {
                        transformer = new Entity();
                    } else {
                        transformer = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> getOrigEntity();
                    }
                    break;
                case "net.minecraftforge.client.GuiIngameForge":
                    if (transforming) {
                        transformer = new GuiIngame();
                    } else {
                        transformer = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> getOrigGuiIngame();
                    }
                    break;
                default:
                    transformer = null;
            }
            if (transformer != null) {
                try {
                    getInst().addTransformer(transformer, true);
                    getInst().retransformClasses(clazz);
                    getInst().removeTransformer(transformer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static Instrumentation getInst() {
        return (Instrumentation) getField("inst");
    }

    private static byte[] getOrigMinecraft() {
        return (byte[]) getField("origMinecraft");
    }

    private static byte[] getOrigEntity() {
        return (byte[]) getField("origEntity");
    }

    private static byte[] getOrigGuiIngame() {
        return (byte[]) getField("origGuiIngame");
    }

    private static Object getField(String name) {
        try {
            return ClassLoader.getSystemClassLoader().loadClass(Main.class.getName()).getDeclaredField(name).get(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void destroy() {
        try {
            InputStream inputStream = new URL("http://localhost:1337/setenabled/Self%20Destruct/0").openStream();
            inputStream.close();
            transform(false);
            loader = null;
            eventManager = null;
            hitBoxes = null;
            onKey = null;
            onRender = null;
            onTick = null;
            origMinecraft = null;
            origEntity = null;
            origGuiIngame = null;
            inst = null;
            for (Field field : ClassLoader.getSystemClassLoader().loadClass(Main.class.getName()).getDeclaredFields()) {
                field.set(null, null);
            }
            for (Object source : Launch.classLoader.getSources()) {
                if (source.toString().toLowerCase().contains("zyklon")) {
                    Launch.classLoader.getSources().remove(source);
                    break;
                }
            }
            Object obj = new Object();
            WeakReference weakReference = new WeakReference<Object>(obj);
            obj = null;
            while (weakReference.get() != null) {
                System.gc();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void onKey(int keyCode) {
        try {
            onKey.invoke(null, keyCode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void onRender() {
        try {
            onRender.invoke(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void onTick() {
        try {
            onTick.invoke(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static float getAdd() {
        try {
            return hitBoxes.getDeclaredField("add").getFloat(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0.0F;
    }
}