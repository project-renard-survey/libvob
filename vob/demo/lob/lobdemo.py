# 
# Copyright (c) 2005, Benja Fallenstein
#
# This file is part of Libvob.
# 
# Libvob is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# Libvob is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.
# 
# You should have received a copy of the GNU General
# Public License along with Libvob; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA  02111-1307  USA
# 

from org.nongnu.libvob.lob import *

from java.awt import Color

import vob, java

class Table(TableLob.Table):
    def getRowCount(self): return 10
    def getColumnCount(self): return 10

    def getLob(self, row, col):
        return Lobs.filledRect(Color(50 + row*10, 0, 50 + col*10))
        #return Lobs.rect(Color.red, 2)

class Scene:
    def key(self, k):
        vob.AbstractUpdateManager.chg()
        
    def mouse(self, m): pass

    def scene(self, scene):
        scene.put(background((1,1,.8)))

        lob = TableLob.newInstance(Table())
        layout = lob.layout(400, 300)

        cs = scene.coords.translate(0, 100, 100)
        layout.render(scene, cs, 0, 1, 1)

        print 'scene rendered'
        
