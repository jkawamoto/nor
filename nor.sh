#!/bin/sh
# Copyright (C) 2010 Junpei Kawamoto
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
##########################################################################

readonly NOR_JAR="nor.jar"
readonly LIB_DIR="lib"
readonly PLUGIN_DIR="plugin"
CP=.:$NOR_JAR

for lib in `ls $LIB_DIR/*.jar`
do
    CP=$CP:$lib
done

for jar in `ls $PLUGIN_DIR/*.jar`
do
    CP=$CP:$jar
done

#echo $CP
java -cp $CP nor.core.Nor