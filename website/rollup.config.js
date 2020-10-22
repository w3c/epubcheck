import { babel } from '@rollup/plugin-babel';
// import commonjs from '@rollup/plugin-commonjs';
// import { resolve } from '@rollup/plugin-node-resolve';
// import replace from '@rollup/plugin-replace';
import { terser } from 'rollup-plugin-terser';
import {sync as globby} from 'globby'

const isProd = process.env.NODE_ENV === 'production'

const common = {
  plugins: [
    // replace({ DEV_MODE: !isProd }),
    // resolve(),
    // commonjs(),
    babel({
      babelHelpers: 'bundled',
      exclude: 'node_modules/**',
    }),
    isProd && terser()
  ],
  watch: {
    clearScreen: false,
  },
}

const critical = globby('src/_scripts/critical*.js').map(input => Object.assign({
  input,
  output: {
    format: 'iife',
    file: input.replace('src/_scripts/','_site/js/'),
    sourcemap: !isProd,
  }
}, common))

export default critical.concat([
  // Object.assign({
  //   input: 'src/_scripts/site.js',
  //   output: {
  //     format: 'iife',
  //     name: 'site',
  //     file: '_site/js/site.js',
  //     sourcemap: !isProd,
  //   }
  // }, common)
])