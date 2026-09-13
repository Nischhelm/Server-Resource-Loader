# ServerResourceLoader

A mod that lets you override or add recipes, loot tables, structure templates, advancements and mcfunctions by dropping files into the `serverresources/` folder.
As a modpack dev tool this mod strives to be as late as possible, overriding any other modification.

Requires any mod that provides Mixin 0.8+, for example [FermiumBooter](https://www.curseforge.com/minecraft/mc-mods/fermiumbooter), [MixinBootstrap](https://www.curseforge.com/minecraft/mc-mods/mixinbootstrap), [MixinCompat](https://www.curseforge.com/minecraft/mc-mods/mixin-0-7-0-8-compatibility) or any mod shipping Mixin (there are lots).

Inspired by [Resource Loader](https://www.curseforge.com/minecraft/mc-mods/resource-loader).

## Usage

The folder structure is created on first startup. It uses the same pattern as the replaced files.

```
serverresources/
  recipes/
    minecraft/stick.json
    somemod/some_recipe.json
  loot_tables/
    minecraft/chests/simple_dungeon.json
  structures/
    minecraft/igloo/igloo_top.nbt
  advancements/
    minecraft/story/mine_stone.json
  functions/
    mypack/starter_kit.mcfunction
```

## Limitations

- Modified recipes are not synced to clients. All other types only matter on the server.
- There is no handling for overriding `_factories.json` and `_constants.json` for mod recipes, overriding these will just fail.
- Minecrafts `/reload` command will pick up new, changed and removed loot tables, advancements and functions. Recipes and structure templates need a game restart.

## Configs

Includes a toggleable fix for [MC-83039](https://bugs.mojang.com/browse/MC-83039), where end city chests are sometimes replaced by air. This will write a file into `structures/`.

If you want to put files into `serverresources/` that aren't supposed to be used (yet), you can use the config entries `Ignored xyz`.