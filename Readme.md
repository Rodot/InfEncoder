#What is InfEncoder
InfEncoder is part of the Gamebuino project, see [the website](http://gamebuino.com) for more information.

InfEncoder is an utility which allows you to create .INF files, which are used to embed logo, images
and description of your Gamebuino games for them to be shown when you select a game on your Gamebuino.

* Put the generated .INF file along with the game's .HEX file on the SD card.
They must have the same name, all in capitals, and 8 characters max.
For example CRABATOR.HEX (the game binaries) and CRABATOR.INF (the description file).
* Logo must be 19*18px
* Slides must be 84*32px, up to 255 slides is allowed.
* Drag and drop slides to reorganize them
* The first slide is the one shown while the game is loading
* Slides can be used to present the game, tell the story, explain how to play, etc.
By putting this information in the INF you can read it before loading the game,
everything that's in the .INF don't need to be in the .HEX so it frees some space for your code.

#How to run it
InfEncoder is made using java, so you need to have the [Java JRE](http://www.oracle.com/technetwork/java/javase/downloads/index.html) installed on your computer.
Then, simply launch InfEncoder/dist/InfEncoder.jar by double clicking it.

#Gamebuino InfEncoder License

(C) Copyright 2014 Aurélien Rodot. All rights reserved.

The Gamebuino InfEncoder is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
