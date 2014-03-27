import com.mumfrey.liteloader.RenderListener;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiIngameMenu;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ProfilerResult;

public class LiteModProfilerDump implements RenderListener {
    private GuiButton currentButton;

    public String getName() {
        return "Profiler Dump";
    }

    public String getVersion() {
        return "0.1.0";
    }

    public void init(File path) {}

    public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

    public void onRender() {}

    public void onRenderGui(GuiScreen currentScreen) {
        if (currentScreen instanceof GuiIngameMenu) {
            Field buttonListField = null;
            try {
                buttonListField = GuiScreen.class.getDeclaredField("buttonList");
            } catch (NoSuchFieldException e0) {
                try {
                    buttonListField = GuiScreen.class.getDeclaredField("field_73887_h");
                } catch (NoSuchFieldException e1) {
                    try {
                        buttonListField = GuiScreen.class.getDeclaredField("i");
                    } catch (NoSuchFieldException e2) {
                        System.err.println("Fuck you too.");
                        e2.printStackTrace();
                        return;
                    }
                }
            }

            try {
                buttonListField.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<GuiButton> buttonList = (List<GuiButton>) buttonListField.get(currentScreen);
                if (currentButton == null || !buttonList.contains(currentButton)) {
                    currentButton = new GuiButtonDumpProfilerResults(currentScreen);
                    buttonList.add(currentButton);
                }
            } catch (IllegalAccessException e) {
                System.err.println("And you, dear sir.");
                e.printStackTrace();
            }
        }
    }

    public void onRenderWorld() {}

    public void onSetupCameraTransform() {}

    private class GuiButtonDumpProfilerResults extends GuiButton {
        public GuiButtonDumpProfilerResults(GuiScreen screen) {
          super(0xc0ffee, screen.width / 2 - 100, screen.height / 4 + 72 - 16, "Dump profiler results");
        }

        @Override
        protected int getHoverState(boolean overButton) {
            this.enabled = Minecraft.getMinecraft().mcProfiler.profilingEnabled;
            return super.getHoverState(overButton);
        }

        @Override
        public boolean mousePressed(Minecraft mc, int x, int y) {
            if (super.mousePressed(mc, x, y)) {
                this.saveProfilerResults();
                return true;
            }
            return false;
        }

        private void saveProfilerResults() {
            File var4 = new File(Minecraft.getMinecraft().mcDataDir, "debug/profile-results-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + ".txt");
            var4.getParentFile().mkdirs();

            try {
                FileWriter var5 = new FileWriter(var4);
                var5.write(this.getProfilerResults());
                var5.close();
            } catch (Throwable var6) {
                Minecraft.getMinecraft().getLogAgent().logSevereException("Could not save profiler results to " + var4, var6);
            }
        }

        private String getProfilerResults() {
            StringBuilder var4 = new StringBuilder();
            var4.append("---- Minecraft Profiler Results ----\n");
            var4.append("--- BEGIN PROFILE DUMP ---\n\n");
            this.getProfileDump(0, "root", var4);
            var4.append("--- END PROFILE DUMP ---\n\n");
            return var4.toString();
        }

        private void getProfileDump(int par1, String par2Str, StringBuilder par3StringBuilder) {
            @SuppressWarnings("unchecked")
            List<ProfilerResult> var4 = (List<ProfilerResult>) Minecraft.getMinecraft().mcProfiler.getProfilingData(par2Str);

            if (var4 != null && var4.size() >= 3) {
                for (int var5 = 1; var5 < var4.size(); ++var5) {
                    ProfilerResult var6 = var4.get(var5);
                    par3StringBuilder.append(String.format("[%02d] ", new Object[] {Integer.valueOf(par1)}));

                    for (int var7 = 0; var7 < par1; ++var7) {
                        par3StringBuilder.append(" ");
                    }

                    par3StringBuilder.append(var6.field_76331_c);
                    par3StringBuilder.append(" - ");
                    par3StringBuilder.append(String.format("%.2f", new Object[] {Double.valueOf(var6.field_76332_a)}));
                    par3StringBuilder.append("%/");
                    par3StringBuilder.append(String.format("%.2f", new Object[] {Double.valueOf(var6.field_76330_b)}));
                    par3StringBuilder.append("%\n");

                    if (!var6.field_76331_c.equals("unspecified")) {
                        try {
                            this.getProfileDump(par1 + 1, par2Str + "." + var6.field_76331_c, par3StringBuilder);
                        } catch (Exception var8) {
                            par3StringBuilder.append("[[ EXCEPTION " + var8 + " ]]");
                        }
                    }
                }
            }
        }
    };
}
