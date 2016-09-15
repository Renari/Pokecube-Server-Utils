package pokecube.wiki;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.compat.Compat;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.api.terrain.BiomeType;

public class PokecubeWikiWriter
{
    private static PrintWriter out;
    private static FileWriter  fwriter;

    static String              pokemobDir = "https://github.com/Thutmose/Pokecube/wiki/";
    static String              gifDir     = "https://raw.githubusercontent.com/wiki/Thutmose/Pokecube/pokemobs/img/";

    static String formatLinkName(String link, String name)
    {
        return "[" + name + "](" + link + ")";
    }

    static String formatLink(String dir, String name, String ext)
    {
        return "[" + name + "](" + dir + name + ext + ")";
    }

    static void writeWiki()
    {
        int n = 0;
        pokemobDir = "https://github.com/Thutmose/Pokecube/wiki/";
        for (n = 1; n < 750; n++)
        {
            PokedexEntry entry = Database.getEntry(n);
            if (entry != null) outputPokemonWikiInfo2(entry);
        }
        writeWikiPokemobList();
        writeWikiHome();
    }

    static void writeWikiHome()
    {
        try
        {
            String fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml", "Home.md");
            fwriter = new FileWriter(fileName);
            out = new PrintWriter(fwriter);
            out.println("#Welcome to the Pokemob Wiki by Thutmose");
            out.println("##List of Mobs");
            out.println(formatLinkName("pokemobList", "List of Pokemobs"));
            out.println("##List of Blocks");
            out.println("##List of Items");
            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    static void writeWikiPokemobList()
    {
        try
        {
            String fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml", "pokemobList.md");
            fwriter = new FileWriter(fileName);
            out = new PrintWriter(fwriter);
            out.println("#List of Pokemobs Currently in Pokecube");
            out.println("|  |  |  |  |");
            out.println("| --- | --- | --- | --- |");
            int n = 0;
            boolean ended = false;
            int m = 0;
            for (m = 1; m < 750; m++)
            {
                PokedexEntry e = Database.getEntry(m);
                if (e == null) continue;
                ended = false;
                String name = I18n.format(e.getUnlocalizedName());
                out.print("| " + formatLink(pokemobDir, name, ""));
                if (n % 4 == 3)
                {
                    out.print("| \n");
                    ended = true;
                }
                n++;
            }
            if (!ended)
            {
                out.print("| \n");
            }
            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    static void outputPokemonWikiInfo2(PokedexEntry entry)
    {
        try
        {
            String name = I18n.format(entry.getUnlocalizedName());
            String fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml", "pokemobs/" + name + ".md");
            File temp = new File(fileName.replace(entry.getName() + ".md", ""));
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            fwriter = new FileWriter(fileName);
            out = new PrintWriter(fwriter);
            String typeString = WordUtils.capitalize(PokeType.getName(entry.getType1()));
            if (entry.getType2() != PokeType.unknown)
                typeString += "/" + WordUtils.capitalize(PokeType.getName(entry.getType2()));

            // Print links to other pokemon
            PokedexEntry nex = Pokedex.getInstance().getNext(entry, 1);
            PokedexEntry pre = Pokedex.getInstance().getPrevious(entry, 1);
            out.println("| | | ");
            out.println("| --- | --- | ");
            String otherPokemon = "<- | ->";
            String next = "";
            if (nex != entry)
            {
                next = formatLink(pokemobDir, nex.getName(), "");
            }
            String prev = "";
            if (pre != entry)
            {
                prev = formatLink(pokemobDir, pre.getName(), "");
            }
            otherPokemon = "| " + prev + otherPokemon + next + " |";

            out.println(otherPokemon);

            // Print the name and header
            out.println("#" + entry.getName());
            String numString = entry.getPokedexNb() + "";
            if (entry.getPokedexNb() < 10) numString = "00" + numString;
            else if (entry.getPokedexNb() < 100) numString = "0" + numString;
            out.println(" | ");
            out.println("--- | ---");
            out.println("| Type: " + typeString + "\n" + "Number: " + numString + "| \n");
            if (entry.hasShiny)
            {
                out.println("[[" + gifDir + entry.getName() + ".png]]" + "[[" + gifDir + entry.getName() + "S.png]]");
            }
            else
            {
                out.println("[[" + gifDir + entry.getName() + ".png]]");
            }

            // Print the description
            out.println("##Description");
            String desc = entry.getName() + " is a " + typeString + " pokemob.";
            if (entry.canEvolve())
            {
                for (EvolutionData d : entry.evolutions)
                {
                    if (d.evolution == null) continue;
                    String evoString = formatLink(pokemobDir, d.evolution.getName(), "");
                    if (d.level > 0)
                    {
                        evoString += " at Level " + d.level;
                    }
                    else if (d.item != null && d.gender == 0)
                    {
                        evoString += " when given " + d.item.getDisplayName();
                    }
                    else if (d.item != null && d.gender == 1)
                    {
                        evoString += " when male and given " + d.item.getDisplayName();
                    }
                    else if (d.item != null && d.gender == 2)
                    {
                        evoString += " when female and given " + d.item.getDisplayName();
                    }
                    else if (d.traded && d.item != null)
                    {
                        evoString += " when traded and given " + d.item.getDisplayName();
                    }
                    else if (d.happy)
                    {
                        evoString += " when Happy";
                    }
                    else if (d.traded)
                    {
                        evoString += " when traded";
                    }
                    desc += " " + entry.getName() + " Evolves into " + evoString + ".";
                }

            }
            if (entry.evolvesFrom != null)
            {
                String evoString = formatLink(pokemobDir, entry.evolvesFrom.getName(), "");
                desc += " " + entry.getName() + " Evolves from " + evoString + ".";
            }
            out.println(desc);

            // Print move list
            out.println("##Natural Moves List");
            out.println("| Level | Move | ");
            out.println("| --- | --- | ");
            List<String> moves = Lists.newArrayList(entry.getMoves());
            List<String> used = Lists.newArrayList();
            for (int i = 1; i <= 100; i++)
            {
                List<String> newMoves = entry.getMovesForLevel(i, i - 1);
                if (!newMoves.isEmpty())
                {
                    for (String s : newMoves)
                    {
                        out.println("| " + (i == 1 ? "-" : i) + "| " + MovesUtils.getLocalizedMove(s) + "| ");
                        for (String s1 : moves)
                        {
                            if (s1.equalsIgnoreCase(s)) used.add(s1);
                        }
                    }
                }
            }
            moves.removeAll(used);

            if (moves.size() > 0)
            {
                out.println("##TM or Egg Moves List");
                out.println("|  |  |  |  |");
                out.println("| --- | --- | --- | --- |");
                boolean ended = false;
                int n = 0;
                for (String s : moves)
                {
                    ended = false;
                    out.print("| " + MovesUtils.getLocalizedMove(s));
                    if (n % 4 == 3)
                    {
                        out.print("| \n");
                        ended = true;
                    }
                    n++;
                }
                if (!ended)
                {
                    out.print("| \n");
                }
            }
            if (!entry.related.isEmpty())
            {
                out.println("##Compatable for Breeding");
                out.println("|  |  |  |  |");
                out.println("| --- | --- | --- | --- |");
                int n = 0;
                boolean ended = false;
                for (PokedexEntry e : entry.related)
                {
                    if (e == null) continue;
                    ended = false;
                    out.print("| " + formatLink(pokemobDir, e.getName(), ""));
                    if (n % 4 == 3)
                    {
                        out.print("| \n");
                        ended = true;
                    }
                    n++;
                }
                if (!ended)
                {
                    out.print("| \n");
                }
            }
            SpawnData data = entry.getSpawnData();
            if (data == null && Database.getEntry(entry.getChildNb()) != null)
            {
                data = Database.getEntry(entry.getChildNb()).getSpawnData();
            }
            if (data != null)
            {
                out.println("##Biomes Found in");
                out.println("|  |  |  |  |");
                out.println("| --- | --- | --- | --- |");
                int n = 0;
                boolean ended = false;
                boolean hasBiomes = false;
                Map<SpawnBiomeMatcher, SpawnEntry> matchers = data.matchers;
                List<String> biomes = Lists.newArrayList();
                for (SpawnBiomeMatcher matcher : matchers.keySet())
                {
                    String biomeString = matcher.spawnRule.values.get(SpawnBiomeMatcher.BIOMES);
                    typeString = matcher.spawnRule.values.get(SpawnBiomeMatcher.TYPES);
                    if (biomeString != null) hasBiomes = true;
                    else if (typeString != null)
                    {
                        String[] args = typeString.split(",");
                        BiomeType subBiome = null;
                        for (String s : args)
                        {
                            for (BiomeType b : BiomeType.values())
                            {
                                if (b.name.replaceAll(" ", "").equalsIgnoreCase(s))
                                {
                                    subBiome = b;
                                    break;
                                }
                            }
                            if (subBiome == null) hasBiomes = true;
                            subBiome = null;
                            if (hasBiomes) break;
                        }
                    }
                    if (hasBiomes) break;
                }
                if (hasBiomes) for (ResourceLocation key : Biome.REGISTRY.getKeys())
                {
                    Biome b = Biome.REGISTRY.getObject(key);
                    if (b != null)
                    {
                        if (data.isValid(b)) biomes.add(b.getBiomeName());
                    }
                }
                for (BiomeType b : BiomeType.values())
                {
                    if (data.isValid(b))
                    {
                        biomes.add(b.readableName);
                    }
                }
                for (String s : biomes)
                {
                    ended = false;
                    out.print("| " + s);
                    if (n % 4 == 3)
                    {
                        out.print("| \n");
                        ended = true;
                    }
                    n++;
                }
                if (!ended)
                {
                    out.print("| \n");
                }
            }

            if (!entry.forms.isEmpty())
            {
                out.println("##Alternate Formes");
                for (PokedexEntry entry1 : entry.forms.values())
                {
                    typeString = WordUtils.capitalize(PokeType.getName(entry1.getType1()));
                    if (entry1.getType2() != PokeType.unknown)
                        typeString += "/" + WordUtils.capitalize(PokeType.getName(entry1.getType2()));
                    // Print the name and header
                    out.println("##" + entry1.getName());
                    out.println("| |");
                    out.println("| --- |");
                    out.println("| Type: " + typeString + " |");
                    if (entry1.hasShiny)
                    {
                        out.println("[[" + gifDir + entry1.getName() + ".png]]" + "[[" + gifDir + entry1.getName()
                                + "S.png]]");
                    }
                    else
                    {
                        out.println("[[" + gifDir + entry1.getName() + ".png]]");
                    }
                }
            }

            out.println(formatLinkName("pokemobList", "List of Pokemobs") + "-------" + formatLinkName("Home", "Home")
                    + "\n");
            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static boolean            gifCaptureState;
    public static boolean             gifs           = true;
    private static int                currentCaptureFrame;
    private static int                currentPokemob = 1;
    private static int                numberTaken    = 1;
    private static int                WINDOW_XPOS    = 1;
    private static int                WINDOW_YPOS    = 1;
    private static int                WINDOW_WIDTH   = 200;
    private static int                WINDOW_HEIGHT  = 200;
    private static List<PokedexEntry> sortedEntries  = Lists.newArrayList();
    private static int                index          = 0;

    static private void openPokedex()
    {
        Minecraft.getMinecraft().thePlayer.openGui(WikiWriteMod.instance, 0,
                Minecraft.getMinecraft().thePlayer.getEntityWorld(), 0, 0, 0);
    }

    static private void setPokedexBeginning()
    {
        if (!gifs)
        {
            index = 0;
            sortedEntries.clear();
            sortedEntries.addAll(Database.allFormes);
            Collections.sort(sortedEntries, new Comparator<PokedexEntry>()
            {
                @Override
                public int compare(PokedexEntry o1, PokedexEntry o2)
                {
                    int diff = o1.getPokedexNb() - o2.getPokedexNb();
                    if (diff == 0)
                    {
                        if (o1.base && !o2.base) diff = -1;
                        else if (o2.base && !o1.base) diff = 1;
                    }
                    return diff;
                }
            });
            return;
        }
        GuiGifCapture.pokedexEntry = Pokedex.getInstance().getEntry(1);

    }

    static private void cyclePokedex()
    {
        if (!gifs)
        {
            GuiGifCapture.pokedexEntry = sortedEntries.get(index++);
            System.out.println(GuiGifCapture.pokedexEntry + " " + index);
            return;
        }
        GuiGifCapture.pokedexEntry = Pokedex.getInstance().getNext(GuiGifCapture.pokedexEntry, 1);
        if (GuiGifCapture.pokedexEntry != null) currentPokemob = GuiGifCapture.pokedexEntry.getPokedexNb();
    }

    static public void beginGifCapture()
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && !gifCaptureState)
        {
            gifCaptureState = true;
            openPokedex();
            setPokedexBeginning();
            System.out.println("Beginning gif capture...");
        }
    }

    static public boolean isCapturingGif()
    {
        return gifCaptureState;
    }

    public static void setCaptureTarget(int number)
    {
        GuiGifCapture.pokedexEntry = Database.getEntry(number);
    }

    static public void doCapturePokemobGif()
    {
        if (gifCaptureState && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            doCapturePokemobGifClient();
        }
    }

    static private void doCapturePokemobGifClient()
    {
        int h = Minecraft.getMinecraft().displayHeight;
        int w = Minecraft.getMinecraft().displayWidth;
        int x = w / 2;
        int y = h / 2;

        WINDOW_XPOS = -250;
        WINDOW_YPOS = -250;
        WINDOW_WIDTH = 120;
        WINDOW_HEIGHT = 120;
        int xb, yb;

        xb = GuiGifCapture.x;
        yb = GuiGifCapture.y;
        int width = WINDOW_WIDTH * w / xb;
        int height = WINDOW_HEIGHT * h / yb;

        x += WINDOW_XPOS;
        y += WINDOW_YPOS;
        if (GuiGifCapture.pokedexEntry != null) currentPokemob = GuiGifCapture.pokedexEntry.getPokedexNb();
        else return;
        String pokename = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml",
                new String("img" + File.separator + currentPokemob + "_"));
        GL11.glReadBuffer(GL11.GL_FRONT);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        String currentFrameSuffix = new String();

        if (currentCaptureFrame < 10) currentFrameSuffix = "0";

        String shinysuffix = GuiGifCapture.shiny && GuiGifCapture.pokedexEntry.hasShiny ? "S" : "";

        currentFrameSuffix += currentCaptureFrame + shinysuffix + ".png";
        String fileName = pokename + currentFrameSuffix;
        if (!gifs) fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml",
                new String("img" + File.separator + GuiGifCapture.pokedexEntry.getName() + shinysuffix + ".png"));
        File file = new File(fileName);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                int k = (i + (width * j)) * 4;
                int r = buffer.get(k) & 0xFF;
                int g = buffer.get(k + 1) & 0xFF;
                int b = buffer.get(k + 2) & 0xFF;
                image.setRGB(i, height - (j + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try
        {
            ImageIO.write(image, "png", file);
            System.out.println("Attempting to write " + file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        currentCaptureFrame++;
        boolean only1 = false;// numberTaken > 3;
        if (currentCaptureFrame > 28 || !gifs)// NUM_CAPTURE_FRAMES)
        {
            currentCaptureFrame = 0;
            numberTaken++;
            if ((gifs && numberTaken >= Pokedex.getInstance().getEntries().size())
                    || (!gifs && index >= sortedEntries.size()) || only1)// ;//NUM_POKEMOBS)
            {
                currentPokemob = 1;
                numberTaken = 1;
                gifCaptureState = false;
                System.out.println("Gif capture complete!");
            }
            else cyclePokedex();
        }
    }
}