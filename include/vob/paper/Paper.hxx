/*
Paper.hxx
 *    
 *    Copyright (c) 2003, Janne Kujala and Tuomas J. Lukka
 *    
 *    This file is part of Libvob.
 *    
 *    Libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *    
 */
/*
 * Written by Janne Kujala and Tuomas J. Lukka
 */

#ifndef GZZ___PAPEROBJ_HXX__
#define GZZ___PAPEROBJ_HXX__

#include <iostream>
#include <string>
#include <vector>
using std::string;
using std::vector;

#include <boost/shared_ptr.hpp>
using boost::shared_ptr;

#include "callgl.hxx"
#include "callgl_objects.hxx"

#include <vob/Vec23.hxx>

#include <vob/IndirectTexture.hxx>

namespace Vob {

/** A class for rendering parts
 * of infinite planes with affine mappings of texture coordinates.
 */
namespace Paper {
    using namespace CallGL;

    /** Specifies texture coordinate system and 
     * the parameters of the diffuse light for
     * lighting the backgrounds using bump mapping.
     */
    class LightParam {
    public:
      /** Paper coordinate system basis vectors and origin in object 
       * coordinates.
       * Paper coordinates x, y, (and z) are mapped to 
       * object position (orig + x e0 + y e1 + z e2).
       * For the eye-linear TexGens, the basis and origin vectors 
       * are transformed
       * to eye coordinates using the modelview matrix in effect 
       * at the time the 
       * TexGen::setUp method is called. 
       * Thus, after setup, the user can switch to a different coordinate
       * system for drawing the paper vertices.
       * e0, e1, and e2 should be orthogonal and the same length. */
      ZVec e0, e1, e2, orig;

      /** Light position in object coordinates.
       * Light position should be given in the same coordinate system as 
       * the basis vectors above. */
      ZVec Light;

      /** Light position w component. 
       * Basically, Light_w should be 1.0 for finite light and
       * 0.0 for infinite light */
      float Light_w;

      /** Light color */
      float color[4];
    };

    /** (internal): an abstract base class
     * for objects used to set up 
     * the texture blending using the light parameters.
     */
    class LightSetup {
    public:
      virtual void setUp(LightParam *param) = 0;
    };

    /** Set up the blend color to be the light color.
     */
    class BlendColorSetup : public LightSetup {
    public:
      virtual void setUp(LightParam *param) {
	glBlendColor(param->color[0], 
		     param->color[1], 
		     param->color[2], 
		     param->color[3]);
      }
    };

    /** Computes light direction in paper texture coordinates
     * into the primary color.
     * Used for DOT3 bump mapping with infinite light.
     * The tex_mat parameter is the TexGen matrix of the paper texture
     */
    class LightDirSetup : public LightSetup {
    protected:
      float mat[4];

    public:
      LightDirSetup(const float *tex_mat) ;

      virtual void setUp(LightParam *param) ;
    };

  /** Simple, non-lighting-dependent TexGen.
   * Transforms paper coordinates into texture coordinates using
   * the given matrix.
   */
  class TexGen {
  protected:
    /** Matrix used to transform paper position into texture coordinates. */
    // XXX: Currently the last four values are always initialized to 0,0,0,1
    float tex_mat[16];
    
  public:
    /** Pointer to current translation matrix used in 
     * texcoords_explicit(). 
     */
    const float *explicit_mat;
      
    /** Creates a new TexGen.
     * @param tex_mat A float vector of 12 elements, 
     * components of s, t and r vectors, respectively.
     */
    TexGen(const float *tex_mat);

    /*** TexGen version of the TexGen setUp. */
    virtual void setUp_texgen(LightParam *param);
    /*** Vertex program version of the TexGen setUp. */
    virtual void setUp_VP(int unit, LightParam * param);
    /*** Explicit version of the TexGen setUp. */
    virtual void setUp_explicit(LightParam *param);

    /*** Generates a vertex program for requested unit */
    virtual string getVPCode(int unit);
  };

  /** TexGen for embossing.
   * Shifts the texture towards the light by an amount proportional to eps.
   * Take the difference of the filtered values generated by two emboss 
   * texgens using the same texture with +eps and -eps parameters
   * to obtain an approximation of the diffuse dot product.
   */
  class TexGenEmboss : public TexGen {
  protected:
    /** Amount to shift the texture towards the light. */
    float eps;
    /**  When embossing (depends of LightParam), texcoords_explicit() needs 
     * a different matrix to transform paper position into embossed 
     * texture coordinates.
     */
    float explicit_tmp_mat[16];

  public:
    /** Creates a new TexGen for embossing.
     * @param tex_mat A float vector of 12 elements, 
     * components of s, t and r vectors, respectively.
     * @param eps mount to shift the texture towrds the light
     */
    TexGenEmboss(const float *tex_mat, float eps) 
      : TexGen(tex_mat), eps(eps) {}

    /*** TexGen version of the TexGenEmboss setUp. */
    virtual void setUp_texgen(LightParam *param) ;
    /*** Vertex program  version of the TexGenEmboss setUp. */
    virtual void setUp_VP(int unit, LightParam * param);
    /*** Explicit version of the TexGenEmboss setUp. */
    virtual void setUp_explicit(LightParam * param);
  };

    /** TexGen for transforming light intensity or direction map 
     * into paper texture coordinates for a light at a finite distance.
     * Intensity map is used with emboss bump mapping and
     * direction map with DOT3 bump mapping.
     * The inv_mat parameter is the TexGen matrix of the paper.
     */
    class TexGenLightmap : public TexGen {
    protected:
      float inv_mat[4];

    public:
      TexGenLightmap(const float *tex_mat, 
		     const float *inv_mat) ;

      virtual void setUp(LightParam *param) ;
    };
  
    /** A single rendering pass.
     * The data members are public to allow modification,
     * but when using a ready-made PaperPass, _choose one_
     * (and only one) of the following ways to use it:
     *
     *   1) using texgen
     *      - call setUp_texgen() with LightParam to use
     *      - call glVertex*() directly from the context
     *      - teardown_texgen()
     *
     *   2) using a vertex program
     *      - call setUp_VP with LightParam to use
     *      - call glTexCoord*() and glVertex*() directly from 
     *        the context _or_ call vertex_VP() with position
     *        within the paper in array of 4 floats
     *      - call teardown_VP()
     *
     *   3) using explicit coordinates
     *     - call setUp_explicit with LightParam to use
     *     - call texcoords_explicit() with paper coordinates in
     *       arrays of 4 floats and call glVertex*() directly
     *       from the context.
     *     - call teardown_explicit()
     */
  class PaperPass {
  private:
    /** Generates and loads the texgen vertex program.
     * This is automatically called on first setUp_VP 
     * unless it's  already loaded.
     */
    void loadVP();

    /** Call setupcode and indirect texture binds.
     */
    void independentSetup();
    /** Call teardown and indirect texture binds.
     */
    void independentTeardown();
    
  public:
    /** The code to call before beginning to render the pass. */
    CallGLCode setupcode;
    /** The code to call after rendering the pass. */
    CallGLCode teardowncode;

    
    /** The indirect textures to be bound for this pass.
     */
    vector<shared_ptr<IndirectTextureBind> > indirectTextureBinds;

    /** The TexGen objects for the different texture units. */
    vector<shared_ptr<TexGen> > texgen;
    /** The non-texgen light setup routines. */
    vector<shared_ptr<LightSetup> > setup;
	  
    /*** TexGen version of the renderinf interface. */
    /** Calls setupcode, texgen and setup for the texture.
     *
     * @param param light parameters (LightParam object)
     */
    void setUp_texgen(LightParam *param);

    /** Calls teardowncode. 
     */
    void tearDown_texgen () { independentTeardown(); }

    /*** Vertex program version of the rendering interface. */
    /** Calls setupcode, texgen and setup for the texture.
     *	  
     * @param param light parameters (LightParam object)
     */
    void setUp_VP(LightParam *param);

    /** Calls teardowncode. 
     */
    void tearDown_VP();

    /** Vertex program version of the paperpass vertex.
     * Calling vertex_VP() is optional. It's possible to call
     * glTexCoord*() and glVertex*() directly from the context
     * and choose the best function variant - ie. use vertex arrays
     * (vertex_VP() forces to pass coordinates in arrays of 4 floats).
     *
     * @param pos vertex position (array of 4 floats)
     * @param ppos position within paper (array of 4 floats)
     */
    void vertex_VP(float *pos, float *ppos) {
      texcoords_VP(ppos);
      glVertex4fv(pos);
    }
    void texcoords_VP(float *ppos) {
      glTexCoord4fv(ppos);
    }
	  
    /*** Explicit version of the rendering interface. */
    /** Calls setupcode, texgen and setup for the texture.
     *	  
     * @param param light parameters (LightParam object)
     */
    void setUp_explicit(LightParam *param);

    /** Calls teardowncode.
     */
    void tearDown_explicit () { independentTeardown(); }

    /** Explicit version of the PaperPass texcoords.
     */
    void texcoords_explicit(float *ppos);
	  
  protected:
    /* Vertex program code. */
#ifdef GL_ARB_vertex_program
    ARBVertexProgram texgenvp;
#endif
  };
  
  /** A paper is simply a vector of passes.
   */
  class Paper : public vector<PaperPass> {};
  
}
}

#endif

